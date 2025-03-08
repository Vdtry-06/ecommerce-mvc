package vdtry06.springboot.ecommerce.address;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.user.User;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String country;
    String city;
    String district;
    String ward;
    String street;
    String houseNumber;

    @OneToOne(mappedBy = "address")
    User user;

}
