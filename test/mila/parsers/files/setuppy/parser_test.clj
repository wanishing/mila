(ns mila.parsers.files.setuppy.parser-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [mila.parsers.files.setuppy.parser :refer [parse-package-info
                                                       parse-packages
                                                       parse-config
                                                       parse-setup-py]]))


(deftest package-info-parsing

  (testing "should return nil on empty input"
    (is (nil? (parse-package-info "")))
    (is (nil? (parse-package-info nil))))

  (testing "should parse package name"
    (is (= (:name (parse-package-info "some-package==1.2.3"))
           "some-package"))
    (is (= (:name (parse-package-info "some-e2e-package==1.2.3"))
           "some-e2e-package"))
    (is (= (:name (parse-package-info "some_e2e_package==1.2.3"))
           "some_e2e_package")))

  (testing "should parse package version"
    (is (= (:version (parse-package-info "some-package==1.2.3"))
           {:major "1"
            :minor "2"
            :patch "3"}))
    (is (= (:version (parse-package-info "some-package==123.456.789"))
           {:major "123"
            :minor "456"
            :patch "789"})))

  (testing "should parse package qualifier"
    (is (= (:qualifier (parse-package-info "some-package==1.2.3"))
           "=="))))

(deftest packages-parsing

  (testing "should return empty list on empty input"
    (is (= (parse-packages nil) []))
    (is (= (parse-packages "") [])))

  (testing "should parse quoted packages"
    (is (= (parse-packages "'trainer-client==2.2.14'")
           [{:name      "trainer-client",
             :qualifier "==",
             :version   {:major "2",
                         :minor "2",
                         :patch "14"}}])))

  (let [expected-packages [{:name      "trainer-client",
                   :qualifier "==",
                   :version   {:major "2", :minor "2", :patch "14"}}
                  {:name      "deepchecks",
                   :qualifier "==",
                   :version   {:major "0", :minor "8", :patch "3"}}
                  {:name      "riskipop",
                   :qualifier "==",
                   :version   {:major "2", :minor "10", :patch "18"}}
                  {:name "mlops_e2e_tools",
                   :qualifier "==",
                   :version {:major "0", :minor "1", :patch "17"}}]]

    (testing "should parse comma delimited sequence of packages"
      (is (= (parse-packages "'trainer-client==2.2.14','deepchecks==0.8.3','riskipop==2.10.18','mlops_e2e_tools==0.1.17'") expected-packages)))

    (testing "should parse comma delimited sequence of packages with whitespaces"
      (is (=  (parse-packages "'trainer-client==2.2.14',
        'deepchecks==0.8.3',
        'riskipop==2.10.18',
        'mlops_e2e_tools==0.1.17'") expected-packages)))))

(deftest config-parsing

  (testing "should return empty list on empty packages"
    (is (= (parse-config "") []))
    (is (= (parse-config nil) []))
    (is (= (parse-config "install_requires=[]") [])))

  (testing "should parse packages within the value"
    (let [expected [{:name      "trainer-client",
                     :qualifier "==",
                     :version   {:major "2", :minor "2", :patch "14"}}
                    {:name      "deepchecks",
                     :qualifier "==",
                     :version   {:major "0", :minor "8", :patch "3"}}
                    {:name      "riskipop",
                     :qualifier "==",
                     :version   {:major "2", :minor "10", :patch "18"}}]]
      (is (=   (parse-config "install_requires=['trainer-client==2.2.14','deepchecks==0.8.3','riskipop==2.10.18']") expected))
      (is (=   (parse-config "install_requires=['trainer-client==2.2.14',
        'deepchecks==0.8.3',
        'riskipop==2.10.18'
    ]")  expected))))
  )

(deftest setup-py-parsing

  (testing "should return empty list on empty packages"
    (is (= (parse-setup-py "") []))
    (is (= (parse-setup-py nil) []))
    (is (= (parse-setup-py "setup()") []))
    (is (= (parse-setup-py "setup(install_requires=[])") [])))

  (let [expected [{:name "trainer-client",
                   :qualifier "==",
                   :version {:major "1",
                             :minor "2",
                             :patch "3"}}]]

    (testing "should parse packages within install_requires"
      (is (= (parse-setup-py "setup(install_requires=['trainer-client==1.2.3'])")
             expected)))

    (testing "should ignore other configurations other than install_requires"
      (is (= (parse-setup-py "setup(
    name='trainer',
    packages=find_packages(),
    version=package_params[\"version\"],
    description='A package for auto training pipelines of chargeback guarantee based machine learning models',
    author='ML Algo Team',
    install_requires=[
        'trainer-client==2.2.14'
    ],
    license='Riskified',
)")
             expected))))


  )
