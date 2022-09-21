def BRANCH_NAME = 'main'
def prepareStages(String name) {
	def tasks = [:]
	tasks["prepare_UC00"] = {
	  stage ("prepare_UC00"){    
		node("${name}") {
			dir("${env.custom_var}"){
				
					sh 'echo -----------------1'
					//sh './test.sh UC01_run'
					sh './start_test_on_slave.sh scripts/UC00.jmx jmeter-0 profile_prepare'
					//sh './profile_run.sh profile_prepare UC00'	
						

			}
		}
	  }
	}
return tasks
}




def runStages(String name, String profile) {
	def tasks = [:]
	tasks["task_1"] = {
	  stage ("task_1"){    
		node("${name}") {
			dir("${env.custom_var}"){
				if(P_UC01.toString()=="${name}"){
					//sh "./profile_run.sh ${profile} UC01"
					sh './start_test_on_slave.sh scripts/UC01.jmx jmeter-0 profile_confirm'
				}		
			}
		}
	  }
	}
	tasks["task_2"] = {
	  stage ("task_2"){    
		node("${name}") {  
			dir("${env.custom_var}"){
				if(P_UC02.toString()=="${name}"){
					sh './start_test_on_slave.sh scripts/UC02.jmx jmeter-1 profile_confirm'
				}
			}
		}
	  }
	}
	tasks["task_3"] = {
	  stage ("task_3"){    
		node("${name}") {  
			dir("${env.custom_var}"){
				if(P_UC03.toString()=="${name}"){
					sh './start_test_on_slave.sh scripts/UC03.jmx jmeter-2 ${profile}'
				}
			}
		}
	  }
	}
	tasks["task_4"] = {
	  stage ("task_4"){    
		node("${name}") {  
			dir("${env.custom_var}"){
				if(P_UC04.toString()=="${name}"){
					sh './start_test_on_slave.sh scripts/UC04.jmx jmeter-3 ${profile}'
				}
			}
		}
	  }
	}
return tasks
}



pipeline {
  parameters { 
    choice(
      name: 'P_PROFILE',
      description: '',
      choices: ['profile_confirm'] as List
    )
    choice(
      name: 'P_SLAVE1',
      description: '',
      choices: ['enable', 'NULL'] as List
    )
    choice(
      name: 'P_UC01',
      description: '',
      choices: ['slave1', 'NULL'] as List
    )	
    choice(
      name: 'P_UC02',
      description: '',
      choices: ['slave1', 'NULL'] as List
    )		
    choice(
      name: 'P_UC03',
      description: '',
      choices: ['slave1', 'NULL'] as List
    )
    choice(
      name: 'P_UC04',
      description: '',
      choices: ['slave1', 'NULL'] as List
    )	
    choice(
      name: 'P_HTTP_ON_SLAVE1',
      description: '',
      choices: ['enable', 'NULL'] as List
    )	
  }  
  
  
  
  agent none
  options {
    skipDefaultCheckout()
    buildDiscarder(logRotator(
      numToKeepStr: '10' + (BRANCH_NAME == 'dev' ? '' : '0'),
      daysToKeepStr: '10' + (BRANCH_NAME == 'dev' ? '' : '0'),
    ))
  }
  stages {
    stage('Build On slave1') {
	when {
		beforeAgent true;
	    expression {
            return P_SLAVE1.toString()!='NULL';
        }        
    }
		agent {
            label 'slave1'
        }
		steps {
			script {
				scmInfo = checkout scm
				f = fileExists 'README.md'
				echo "f=${f}"
				sh 'chmod +x build.sh'
				sh 'chmod +x start_test_on_slave.sh'
				//sh './build.sh'
			}		
		}	
	}


    stage('HTTPserver On slave1') {
	when {
		beforeAgent true;
	    expression {
            return P_HTTP_ON_SLAVE1.toString()!='NULL';
        }        
    }
		agent {
            label 'slave1'
        }
		steps {
			script {
				//sh 'kubectl exec -ti -n alrosa jmeter-0 -- bash -c "netstat -tunlp|grep 8089|awk \'{print \\$7}\'|awk -F \"/\" \'{print \\$1}\'|xargs kill -9"'
				sh 'kubectl cp HTTPserver/ -n alrosa jmeter-0:/opt/apache-jmeter-5.4/bin/HTTPserver/'				
				//sh 'nohup kubectl exec -ti -n alrosa jmeter-0 -- bash -c "JENKINS_NODE_COOKIE=dontKillMe java -jar /opt/apache-jmeter-5.4/bin/HTTPserver/HTTPserver/dist/HTTPserver.jar" &'		
			}		
		}	
	}




    stage('Prepare') {
		parallel {
			stage('Prepare On slave1') {
				when {
					beforeAgent true;
					expression {
						return P_SLAVE1.toString()!='NULL';
					}        
				}
			    agent {
                   label 'slave1'
                }				
				steps {						
					script {					
						def workspace = "${env.WORKSPACE}"
						echo "Current workspace "+workspace
						env.custom_var=workspace
						currtasks1 =  prepareStages("slave1")
							stage('prepareStages') {
								parallel currtasks1

							}
						
					}	
				}
			}
		
		}	
	}
	
    stage('Tests') {
		parallel {
			stage('Test On slave1') {
				when {
					beforeAgent true;
					expression {
						return P_SLAVE1.toString()!='NULL';
					}        
				}
			    agent {
                   label 'slave1'
                }				
				steps {						
					script {
						echo "Current workspace is ${env.WORKSPACE}"
						def workspace = "${env.WORKSPACE}"
						echo "Current workspace "+workspace
						env.custom_var=workspace
						currtasks1 =  runStages("slave1", P_PROFILE.toString())
							stage('Testt') {
								parallel currtasks1
							}					
					}	
				}
			}					
			
		}	
	}
  }
 
}
