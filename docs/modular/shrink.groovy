import com.google.common.base.Charsets
import com.google.common.io.Files

import java.nio.file.Paths

def lines = Files.readLines(
        Paths.get('/Users/valentyn.berezin/IdeaProjects/my-fork-datasafe/docs/modular/create_user.puml').toFile(),
        Charsets.UTF_8
)