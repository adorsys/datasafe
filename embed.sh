#!/bin/bash

# Embeds snippets into markdown
# usage: ./embed.sh embedding_anchor file_with_markdown.md
# if embedding_anchor is i.e. 'Example' then line in file_with_markdown.md:
# [Example: Creating default Datasafe services](some/path/dir/file.java)
# means go to some/path/dir/file.java to find snippet 'Creating default Datasafe services' and embed it.
# snippet format is:
# BEGIN_SNIPPET:Creating default Datasafe services
# END_SNIPPET

EMBEDDING_ANCHOR=$1
MARKDOWN_TARGET=$2
ANCHOR_PATTERN='\['"$EMBEDDING_ANCHOR"':([^]]+)]\(([^)^#]+)[^)]*\)'

function cleanup_embedded()
{
    cat $MARKDOWN_TARGET \
        | tr '\n' '\f' \
        | sed -r 's/```/\a/g' \
        | sed -E 's/('"$ANCHOR_PATTERN"')(\f\a[^\a]+\a)/\1/g'  \
        | sed -r 's/\a/```/g' \
        | tr '\f' '\n'
}

function wrap_snippet() # expects 2 args - snippet code and file exension
{
    snippet=$1
    extension=$2

    started=false
    spaces=0
    echo '```'"$extension"
    # remove leading spaces:
    while IFS= read -r line; do
        if [[ "$started" = false ]]; then
            no_trailing=$(sed -e 's/^[ \t]*//' <<< "$line")
            let spaces=${#line}-`echo "$no_trailing" | wc -c`+1
            started=true
        fi
        echo "${line:$spaces:${#line}}"
    done <<< "$snippet"
    echo '```'
}

function print_snippet_from_file() # expects 2 args - filename and snippet name
{
    filepath=$1
    snippet_name=$2
    filename=$(basename -- "$filepath")
    extension="${filename##*.}"

    # in snippets it can be that some parts of context is missing, so using Groovy for syntax-highlighting
    if [[ "$extension" -eq 'java' ]]; then
        extension='groovy'
    fi

    snippet=$(cat "$filepath" \
        | tr '\n' '\f' \
        | sed -r 's/BEGIN_SNIPPET:/\a/g' \
        | sed -r 's/END_SNIPPET/\a/g' \
        | grep -oP '\a'"$snippet_name\f"'([^\a]+)\a' \
        | sed -E 's/\a'"[^\f]+\f"'([^\a]+)\f[^\f]+\a/\1/g' \
        | sed -r 's/\a//g' \
        | tr '\f' '\n')

    wrap_snippet "$snippet" "$extension"
}

function snippet_position() # expects 2 args - filename and snippet name
{
    filepath=$1
    snippet_name=$2
    begin=`grep -oPn 'BEGIN_SNIPPET:'"$snippet_name"'$' "$filepath" | cut -d: -f1`
    end=`tail -n +"$begin" $filepath | grep -oPn -m 1 'END_SNIPPET' | cut -d: -f1`
    echo "L$begin-L$(($begin+end))"
}

function snippet_position_end() # expects 2 args - filename and snippet beginning position
{
    filepath=$1
    snippet_name=$2
    echo `grep -oPn 'BEGIN_SNIPPET:'"$snippet_name"'$' $filepath | cut -d: -f1`
}

CLEANED=$(cleanup_embedded)

while read -r line; do
    if [[ $line =~ $ANCHOR_PATTERN ]]; then
        filename="${BASH_REMATCH[2]}"
        snippet_name="${BASH_REMATCH[1]}"
        snippet_lines=`snippet_position "$filename" "$snippet_name"`
        echo "[$EMBEDDING_ANCHOR:$snippet_name]($filename#$snippet_lines)"
        print_snippet_from_file "$filename" "$snippet_name"
    else
        echo "$line"
    fi
done <<< "$CLEANED"
