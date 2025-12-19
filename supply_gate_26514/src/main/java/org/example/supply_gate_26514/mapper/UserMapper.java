package org.example.supply_gate_26514.mapper;

import org.example.supply_gate_26514.dto.UserDto;
import org.example.supply_gate_26514.dto.UserResponseDto;
import org.example.supply_gate_26514.model.Location;
import org.example.supply_gate_26514.model.Store;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.model.UserEnum;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public User transformUserToUserDto(UserDto userDto) {
        User user = new User();
        user.setUserType(UserEnum.valueOf(userDto.userType()));
        user.setEmail(userDto.email());
        user.setPassword(userDto.password());
        user.setFirstName(userDto.firstname());
        user.setLastName(userDto.lastname());
        user.setUsername(userDto.username());
        user.setPhoneNumber(userDto.phoneNumber());
        user.setCompanyName(userDto.companyName());
        Location location = new Location();
        location.setStructureId(userDto.locationId());
        user.setLocation(location);
        return user;
    }
    public UserResponseDto transformUserDtoToUserResponseDto(User user) {
        return new UserResponseDto(user.getUsername(), user.getFirstName(), user.getLastName());
    }

}
