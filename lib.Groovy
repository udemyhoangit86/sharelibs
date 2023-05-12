def docker_compose_build(dockerfile) {
    this.docker_compose_exec("-f  ${dockerfile} build ")
}

def docker_compose_up(dockerfile) {
    this.docker_compose_exec("-f  ${dockerfile} up -d --force-recreate ")
}

def docker_compose_down(dockerfile) {
    this.docker_compose_exec("-f  ${dockerfile} down ")
}

def docker_compose_exec(command) {
    this.exec(" docker-compose ${command} ")
}

def exec(command) {
    sh(
            returnStdout: true,
            script: "echo \" ${command} \";set +x;sh -c \" ${command}  \" "
    )
}
def dk_exec(command) {
    sh(
            returnStdout: true,
            script: "echo \" ${command} \";set +x;docker exec ${CONTAINER_NAME} sh -c \" ${command}  \" "
    )
}

def dk_exec_supervisor(command) {
    sh(
            returnStdout: true,
            script: "echo \" ${command} \";docker exec ${CONTAINER_NAME} sh -c \"sudo supervisorctl ${command}  \" "
    )
}

def dk_exec_sw(command) {
  dk_exec ("sudo -Hu www-data ${command}")
}

def git_exec(command, path) {
    dk_exec_sw ("git --git-dir=${path}/.git --work-tree=${path} ${command}")
}

def dk_git_exec_pull(path) {
    dk_exec ("sudo -Hu www-data git --git-dir=${path}/.git --work-tree=${path} fetch origin ${BRANCH} && sudo -Hu www-data git --git-dir=${path}/.git --work-tree=${path} reset --hard origin/${BRANCH}")
}

def git_exec_pull(path) {
    exec ("sudo -Hu www-data git --git-dir=${path}/.git --work-tree=${path} fetch origin ${BRANCH} && sudo -Hu www-data git --git-dir=${path}/.git --work-tree=${path} reset --hard origin/${BRANCH}")
}

def sendTelegram(message) {
    def encodedMessage = URLEncoder.encode(message, "UTF-8")

    withCredentials([string(credentialsId: 'Jenkins-noti-bot', variable: 'TOKEN'),
    string(credentialsId: 'Orgit-group', variable: 'CHAT_ID')]) {
        response = httpRequest (consoleLogResponseBody: true,
                contentType: 'APPLICATION_JSON',
                httpMode: 'GET',
                url: "https://api.telegram.org/bot$TOKEN/sendMessage?text=$encodedMessage&chat_id=$CHAT_ID&parse_mode=html&disable_web_page_preview=true",
                validResponseCodes: '200')
        return response
    }
}

return this
