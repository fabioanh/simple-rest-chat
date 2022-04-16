package com.visable.chat.data

import javax.persistence.*

@Entity(name = "users")
@Cacheable
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val nickname: String
)
