package com.ashish.fileuploadexportsystem.Repositorys;

import com.ashish.fileuploadexportsystem.Models.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDataRepository extends JpaRepository<FileData, Integer> {

} // JPA repo for FileData
