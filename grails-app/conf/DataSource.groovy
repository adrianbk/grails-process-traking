/**
 * Create a mysql db
 CREATE DATABASE ptracking;
 GRANT USAGE ON ptracking.* to ptracking@localhost IDENTIFIED BY 'ptracking';
 GRANT ALL ON ptracking.* to ptracking@localhost;
 */

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

dataSource_processTracking{
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
//My SQL
//dataSource_processTracking {
//    pooled=true
//    dbCreate='update'
//    driverClassName='com.mysql.jdbc.Driver'
//    dialect='org.hibernate.dialect.MySQL5Dialect'
//    username='ptracking'
//    password='ptracking'
//    properties {
//        maxActive=8
//        maxIdle=25
//        minIdle=5
//        initialSize=5
//        minEvictableIdleTimeMillis=60000
//        timeBetweenEvictionRunsMillis=60000
//        maxWait=10000
//        validationQuery='SELECT 1'
//    }
//}
environments {

       //My SQL
//    development {
//        dataSource_processTracking{
//            dbCreate = "update" // one of 'create', 'create-drop','update'
//            url = "jdbc:mysql://localhost:3306/ptracking?useUnicode=yes&characterEncoding=UTF-8"
//        }
//    }
//    test {
//        dataSource_processTracking {
////            loggingSql = false
////            formatSql = false
//            dbCreate = "update" // one of 'create', 'create-drop','update'
//            url = "jdbc:mysql://localhost:3306/ptracking?useUnicode=yes&characterEncoding=UTF-8"
//        }
//
//    }

//    In Memory DB
    development {
        dataSource_processTracking {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource_processTracking {
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
        dataSource_processTracking {
            dbCreate = "update"
            url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
            pooled = true
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis=1800000
                timeBetweenEvictionRunsMillis=1800000
                numTestsPerEvictionRun=3
                testOnBorrow=true
                testWhileIdle=true
                testOnReturn=true
                validationQuery="SELECT 1"
            }
        }
    }
}

