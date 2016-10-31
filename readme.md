#Simple phonebook application.

To run app localy:
1) Create postgresql database using script *conf/init.sql*
2) To launch tests run: *sbt test*
3) Start app: *sbt -DJDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/phonebook?user=phonebook&password=password" -DJDBC_DATABASE_USERNAME=phonebook -DJDBC_DATABASE_PASSWORD=password run*
4) Open web browser at *http://localhost:9000*

[App running on Heroku](http://scary-cemetery-59032.herokuapp.com/)
