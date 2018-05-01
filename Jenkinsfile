node() {
    //step([$class: 'StashNotifier'])
    
    // Stage, is to tell the Jenkins that this is the new process/step that needs to be executed
    stage('Checkout') {
        // Pull the code from the repo
        checkout scm
    }
    
    stage('Build') {
      try {
        sh './gradlew --refresh-dependencies clean assemble'
        //lock('emulator') {
        //   sh './gradlew connectedCheck'
        //}
        currentBuild.result = 'SUCCESS'
      } catch(error) {
        slackSend channel: '#build-failures', color: 'bad', message: "This build is broken ${env.BUILD_URL}", token: 'XXXXXXXXXXX'
        currentBuild.result = 'FAILURE'
      } finally {
        junit '**/test-results/**/*.xml'
      }
    }
    stage('Archive') {
      archiveArtifacts 'app/build/outputs/apk/*'
    }
    //step([$class: 'StashNotifier'])
}
