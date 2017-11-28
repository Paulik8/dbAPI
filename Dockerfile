FROM ubuntu:16.04

# Update packages
RUN apt-get -y update

# Install postgresql
ENV PGVER 9.5
RUN apt-get install -y postgresql-$PGVER

USER postgres

RUN /etc/init.d/postgresql start &&\
        psql --command "CREATE USER admin WITH SUPERUSER PASSWORD 'docker';" &&\
         createdb -O admin api &&\
          /etc/init.d/postgresql stop

EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

RUN apt-get install -y openjdk-8-jdk-headless maven

ENV PGUSER=admin PGPASSWORD=docker PGHOST=127.0.0.1 PGPORT=5432 PGDATABASE=api
ENV PARK_DB_ROOT=/var/www/api

RUN mkdir -p $PARK_DB_ROOT
COPY . $PARK_DB_ROOT
WORKDIR $PARK_DB_ROOT

RUN mvn package

EXPOSE 5000

CMD service postgresql start && java -jar $PARK_DB_ROOT/target/maildbjava-1.0-SNAPSHOT.jar