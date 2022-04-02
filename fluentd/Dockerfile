FROM fluent/fluentd:v1.14.5-1.1
# Use root account to use apk
USER root

# below RUN includes plugin as examples elasticsearch is not required
# you may customize including plugins as you wish
RUN apk add --no-cache --update --virtual .build-deps \
        sudo build-base ruby-dev \
 && sudo gem install elasticsearch --no-document --version 7.17.1\
 && sudo gem install fluent-plugin-elasticsearch\
 && sudo gem sources --clear-all \
 && apk del .build-deps \
 && rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem

COPY conf/fluent.conf /fluentd/etc/

USER fluent