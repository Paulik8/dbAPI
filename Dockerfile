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

          RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf
          RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf
          RUN echo "synchronous_commit=off" >> /etc/postgresql/$PGVER/main/postgresql.conf
          RUN echo "fsync = 'off'" >> /etc/postgresql/$PGVER/main/postgresql.conf
          RUN echo "max_wal_size = 1GB" >> /etc/postgresql/$PGVER/main/postgresql.conf
          RUN echo "shared_buffers = 256MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
          RUN echo "effective_cache_size = 256MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
          RUN echo "work_mem = 32MB" >> /etc/postgresql/$PGVER/main/postgresql.conf

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