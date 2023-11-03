package com.catcher.datasource;

import com.catcher.core.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

}
