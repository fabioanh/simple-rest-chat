package com.visable.chat.data

import com.visable.chat.controllers.dtos.UserDto
import javax.persistence.*

@Entity(name = "users")
@Cacheable
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val nickname: String
) {
    fun toDto(): UserDto = UserDto(id, nickname)
}
