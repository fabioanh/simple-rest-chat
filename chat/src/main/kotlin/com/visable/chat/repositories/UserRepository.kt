package com.visable.chat.repositories

import com.visable.chat.data.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long>
