package com.reanit.ws.emailnotification.io;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedVentRepository extends JpaRepository<ProcessedEventEntity, Long>{

}
