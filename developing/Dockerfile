FROM jekyll/builder

WORKDIR /github-pages
RUN chmod -R 777 /github-pages
COPY Gemfile Gemfile
RUN bundle install

EXPOSE 4000
CMD bundle exec jekyll serve\
        --host 0.0.0.0 \
        --source /github-pages/web/source \
        --destination /github-pages/rendered \
        --baseurl=/pages/adorsys/datasafe