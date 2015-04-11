package com.jinloes.activiti_test;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by jinloes on 4/11/15.
 */
public interface PersonRepository extends JpaRepository<Person, UUID> {
    Person findByUsername(String username);
}
