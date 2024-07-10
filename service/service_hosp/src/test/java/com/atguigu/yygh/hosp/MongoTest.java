package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.mongo.UserRepository;
import com.atguigu.yygh.hosp.pojo.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


import java.util.List;
import java.util.Optional;

@SpringBootTest
public class MongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testAdd() {

        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setId(Integer.toString(i));
            user.setName("name"+i);
            user.setAge(20+i);
            mongoTemplate.save(user);
        }

    }

    @Test
    public void testFindAll() {
        List<User> users = mongoTemplate.findAll(User.class);
        System.out.println("users = " + users);
    }

    @Test
    public void testFindById() {
        User byId = mongoTemplate.findById(1, User.class);
        System.out.println("byId = " + byId);
    }


    @Test
    public void testFindUser() {
        Query query = new Query(Criteria.where("name").is("name2").and("age").is(22));
        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println("users = " + users);
    }

    @Test
    public void findUsersLikeName() {
//        String name = "est";
//        String regex = String.format("%s%s%s", "^.*", name, ".*$");
//        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//        Query query = new Query(Criteria.where("name").regex(pattern));
        //简洁写法
        Query query = new Query(Criteria.where("name").regex(".*1.*"));
        List<User> userList = mongoTemplate.find(query, User.class);
        System.out.println(userList);
    }

    @Test
    public void testFindPage() {
        int pageNo = 0;
        int pageSize = 3;

        Query query = new Query(Criteria.where("name").regex(".*"));
        query.addCriteria(Criteria.where("email").is(null));
        //没分页的总数
        long count = mongoTemplate.count(query, User.class);
        //分页条件
        query.skip(pageNo*pageSize).limit(pageSize);

        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println("count = " + count);
        System.out.println("users = " + users);
    }

    @Test
    public void createUser() {
        User user = new User();
        user.setId("11");
        user.setName("李四");
        userRepository.save(user);
        Optional<User> byId = userRepository.findById("11");
        System.out.println("byId = " + byId);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        List<User> one = userRepository.findAll(Example.of(user, matcher));
        System.out.println("one = " + one);
    }

    //分页查询
    @Test
    public void findUsersPage() {
        Sort sort = Sort.by(Sort.Direction.DESC, "age");

        PageRequest pageRequest = PageRequest.of(0, 2, sort);

        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        User user = new User();
        user.setName("1");

        Page<User> all = userRepository.findAll(Example.of(user, matcher), pageRequest);
        System.out.println("all = " + all);
    }
}
