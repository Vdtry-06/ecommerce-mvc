package vdtry06.springboot.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vdtry06.springboot.ecommerce.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
