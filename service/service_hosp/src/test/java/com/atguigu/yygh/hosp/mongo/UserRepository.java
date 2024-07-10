package com.atguigu.yygh.hosp.mongo;

import com.atguigu.yygh.hosp.pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

}