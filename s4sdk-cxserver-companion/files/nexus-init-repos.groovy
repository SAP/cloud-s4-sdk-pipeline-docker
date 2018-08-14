synchronized (this) {
    def result = [created: [], deleted: []]
    def repoManager = repository.repositoryManager
    def existingRepoNames = repoManager.browse().collect { repo -> repo.name }
    def mvnProxyName = 'mvn-proxy'
    if (!existingRepoNames.contains(mvnProxyName)) {
        repository.createMavenProxy(mvnProxyName, '<%= mvn_repository_url %>')
        result.created.add(mvnProxyName)
    }
    def npmProxyName = 'npm-proxy'
    if (!existingRepoNames.contains(npmProxyName)) {
        repository.createNpmProxy(npmProxyName, '<%= npm_registry_url %>')
        result.created.add(npmProxyName)
    }
    def proxyRepos = [npmProxyName, mvnProxyName]
    def toDelete = existingRepoNames.findAll { !proxyRepos.contains(it) }
    for (def repoName : toDelete) {
        repoManager.delete(repoName)
        result.deleted.add(repoName)
    }

    if ('<%= http_proxy %>'?.trim()) {
        URL httpProxy = new URL('<%= http_proxy %>')
        println("httpProxy: ${httpProxy}")
        core.httpProxy(httpProxy.host, httpProxy.port)
    }

    if ('<%= https_proxy %>'?.trim()) {
        URL httpsProxy = new URL('<%= https_proxy %>')
        println("httpsProxy: ${httpsProxy}")
        core.httpsProxy(httpsProxy.host, httpsProxy.port)
    }

    if (('<%= http_proxy %>'?.trim() || '<%= https_proxy %>'?.trim()) && '<%= no_proxy %>'?.trim()) {
        String[] nonProxyHosts = '<%= no_proxy %>'.split(',')
        /* Insert wildcards to have a rough conversion between the unix-like no-proxy list and the java notation.
         * For example, `localhost,.corp,.maven.apache.org,x.y.,myhost` will be transformed to
         * `[*localhost,*.corp,*.maven.apache.org,*x.y,*myhost]`. */
                .collect { it.replaceFirst('\\.$', '')}
                .collect { "*${it}" }
        println("nonProxyHosts: ${nonProxyHosts.join(',')}")
        core.nonProxyHosts(nonProxyHosts)
    }

    return result
}
