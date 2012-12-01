/**
 * Create a mysql db
 CREATE DATABASE ptracking;
 GRANT USAGE ON ptracking.* to ptracking@localhost IDENTIFIED BY 'ptracking';
 GRANT ALL ON ptracking.* to ptracking@localhost;
 */
dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
    pooled=true
    dbCreate='update'
    driverClassName='com.mysql.jdbc.Driver'
    dialect='org.hibernate.dialect.MySQL5InnoDBDialect'
    username='ptracking'
    password='ptracking'
    properties {
        maxActive=8
        maxIdle=25
        minIdle=5
        initialSize=5
        minEvictableIdleTimeMillis=60000
        timeBetweenEvictionRunsMillis=60000
        maxWait=10000
        validationQuery='SELECT 1'
    }
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
environments {

    development {
        dataSource{
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:mysql://localhost:3306/ptracking?useUnicode=yes&characterEncoding=UTF-8"
        }
    }
    test {
        dataSource {
//            loggingSql = false
//            formatSql = false
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:mysql://localhost:3306/ptracking?useUnicode=yes&characterEncoding=UTF-8"
        }

    }
}
