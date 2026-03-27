package com.company.pet_sitter_server.repository;

import com.company.pet_sitter_server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId AND m.receiverId = :otherId) OR (m.senderId = :otherId AND m.receiverId = :userId) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId") Long userId, @Param("otherId") Long otherId);

    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Long> findConversationPartnerIds(@Param("userId") Long userId);
}
