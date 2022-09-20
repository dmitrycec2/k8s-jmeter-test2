def BRANCH_NAME = 'main'
def prepareStages(String name) {
	def tasks = [:]
	tasks["task_1"] = {
	  stage ("task_1"){    
		node("${name}") {
			dir("${env.custom_var}"){
				if(P_UC022.toString()=="${name}"){
					sh 'echo -----------------1'
					//sh './test.sh UC01_run'
					sh './start_test_on_slave.sh scripts/UC022.jmx jmeter-0 profile_max'
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
      name: 'P_SLAVE1',
      description: '',
      choices: ['enable', 'NULL'] as List
    )
    choice(
      name: 'P_UC022',
      description: '',
      choices: ['slave1', 'NULL'] as List
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
						echo "Current workspace is ${workspace}"
						echo "Current workspace "+workspace
						env.custom_var=workspace
						currtasks1 =  prepareStages("slave1")
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
