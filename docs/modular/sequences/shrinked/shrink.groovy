import java.nio.file.Files
import java.nio.file.Paths

REGEX_MAPPING = [
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.profile.+': 'Profile',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.credentials.+': 'Credentials',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.cmsencryption.+': 'CMS',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.dfs.+': 'DFS',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.document.+': 'Document',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.inbox.+': 'INBOX',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.private.+': 'PRIVATE',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.serde.+': 'SERDE',
        'de\\.adorsys\\.dfs\\.connection\\.api\\.service\\.api.+': 'DFS',
        'de\\.adorsys\\.datasafe\\.business\\.api\\.encryption\\.cmsencryption.+': 'CMS',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.cmsencryption\\.services.+': 'CMS',
        '.+deployment\\.profile.+': 'Profile',
        '.+deployment\\.credentials.+': 'Credentials',
        '.+deployment\\.cmsencryption.+': 'CMSEncryption',
        '.+deployment\\..*dfs.+': 'DFS',
        '.+deployment\\.document.+': 'Document',
        '.+deployment\\.inbox.+': 'INBOX',
        '.+deployment\\.private.+': 'PRIVATE',
        '.+deployment\\.serde.+': 'SERDE',
        '.+deployment\\.keystore.+' : 'KeyStore',
        'de\\.adorsys\\.datasafe\\.business\\.impl\\.keystore.+': 'KeyStore'
]

def lines = Files.lines(
        Paths.get('/Users/valentyn.berezin/IdeaProjects/my-fork-datasafe/docs/modular/read_inbox.puml')
).collect {it}

def classFromPackage(String pkg) {
    String[] parts = pkg.split('\\.')
    return parts.length > 1 ? parts[parts.length - 1] : pkg
}

fqn = [:]

lines.forEach {
    if (it.startsWith("' ")) {
        def packageName = it.substring(2)
        fqn[classFromPackage(packageName)] = packageName
    }
}

def mappedLines = []

String replacement(String clazz) {

    for (def it : REGEX_MAPPING) {
        if (clazz.matches(it.key)) {
            return it.value
        }
    }

    return null
}

def replaceIfNeededBracket(String line, String sep) {
    if (!line.contains(sep)) {
        return line
    }

    def parts = line.split(sep)

    def lhs = parts[0].replaceAll('[- ]', '')
    def rhs = parts[1].split(':')[0].replaceAll('[- ]', '')

    if (fqn[lhs]) {
        def repl = replacement(fqn[lhs])
        if (repl) {
            line = line.replaceAll("\\b${lhs}\\b", repl)
        }
    }

    if (fqn[rhs]) {
        def repl = replacement(fqn[rhs])
        if (repl) {
            line = line.replaceAll("\\b${rhs}\\b", repl)
        }
    }

    return line
}

def replaceIfNeededActivateDeactivate(String line) {
    if (!line.contains("activate")) { // matches deactivate too
        return line
    }

    def parts = line.split("activate ")
    def clazz = parts.length > 1 ? parts[1] : parts[0]


    if (!fqn[clazz]) {
        return line
    }

    def repl = replacement(fqn[clazz])

    if (repl) {
        line = line.replaceAll("\\b${clazz}\\b", repl)
    }

    return line
}

lines.forEach {
    def line = replaceIfNeededBracket(it, '<')
    line = replaceIfNeededBracket(line, '>')
    line = replaceIfNeededActivateDeactivate(line)

    mappedLines += line
}

print(mappedLines.join('\n'))