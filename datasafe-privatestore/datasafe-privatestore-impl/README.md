# Datasafe privatespace default implementation
This is the default implementation of datasafe privatespace that encrypts both document and its path. Path is encrypted
in the way it is possible to perform document traversal using encrypted path segments, so that:
path A/B/C will transform into encrypted(A)/encrypted(B)/encrypted(C).

## Writing private file
![Writing details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/private_write.puml&fmt=svg&vvv=1&sanitize=true)

## Reading private file
![Reading details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/private_read.puml&fmt=svg&vvv=1&sanitize=true)