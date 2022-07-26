package backend.service.impl;

import backend.dto.AdminDto;
import backend.dto.RestaurantDto;
import backend.dto.RoleDto;
import backend.dto.UserDto;
import backend.model.Restaurant;
import backend.model.Role;
import backend.model.User;
import backend.repository.RoleRepository;
import backend.service.AdminService;
import backend.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"backend"})
@EnableJpaRepositories(basePackages = { "backend.repository" })
@EntityScan(basePackages = { "backend.model" })
public class AdminServiceImpl implements AdminService {
    private final UserServiceImpl userService;
    private final RestaurantService restaurantService;
    private final RoleRepository roleRepository;

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public AdminServiceImpl(UserServiceImpl userService, RestaurantService restaurantService, RoleRepository roleRepository){
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.roleRepository = roleRepository;
    }
    // saving or updating user for admin (roles, restaurant)
    @Override
    public AdminDto save(AdminDto adminDto) throws Exception {
        User user;
            UserDto userDto;
            if (adminDto.getId() == null)
                //save
                userDto = convertAdminDtoToUserDtoAdd(adminDto);
            else
                //update
                userDto = convertAdminDtoToUserDtoUpdate(adminDto);
            UserDto userDtoSave = userService.save(userDto);
            if (adminDto.getId() == null)
                userDtoSave.setId(userService.nrOfUsers());
            else
                userDtoSave.setId(adminDto.getId());
            user = userService.convertDtoToUserUpdate(userDtoSave);
            //setting roles
            userDtoSave = setRoles(adminDto,user,userDtoSave);
            Integer idRestaurant;
            Restaurant restaurant = null;
            if (adminDto.getRestaurant() != null)
                restaurant = restaurantService.findByName(adminDto.getRestaurant());
            if (restaurant != null)
                idRestaurant = restaurant.getId();
            else
                return convertToAdminDto(userDtoSave);
            //setting restaurant
            return saveRestaurant(userDtoSave,adminDto,idRestaurant);
    }
    private UserDto setRoles(AdminDto adminDto, User user, UserDto userDtoSave) throws Exception{
        RoleDto roleDto = new RoleDto();
        Boolean hasRoles;
        try {
            hasRoles = !adminDto.getRoles().isEmpty();
        }catch (Exception e){
            hasRoles = false;
        }
        if (hasRoles) {
            roleDto.setRoles(adminDto.getRoles());
            user = userService.setRoles(user, roleDto);
            userDtoSave = userService.convertUserToDto(user);
            return userDtoSave;
        }
        //remove added client role
        Role role = roleRepository.findRoleByRole("CLIENT");
        if (user.getRoles().size()>1 && user.getRoles().contains(role)
                && (adminDto.getRoles() == null || adminDto.getRoles().isEmpty()))
        {
            role.getUsers().remove(user);
            user.getRoles().remove(role);
            roleRepository.save(role);
            userService.saveFromRepository(user);
        }
        return userDtoSave;
    }
    private AdminDto saveRestaurant(UserDto userDtoSave, AdminDto adminDto, Integer idRestaurant) throws Exception{
        RestaurantDto restaurantDto = new RestaurantDto();
        restaurantDto.setManager(userDtoSave.getUserName());
        restaurantDto.setName(adminDto.getRestaurant());
        restaurantDto.setId(idRestaurant);
        restaurantService.save(restaurantDto);
        AdminDto adminDto1 = convertToAdminDto(userDtoSave);
        adminDto1.setRestaurant(adminDto.getRestaurant());
        return adminDto1;
    }
    private AdminDto convertToAdminDto(UserDto userDto){
        AdminDto adminDto = new AdminDto();
        adminDto.setUserName(userDto.getUserName());
        adminDto.setFirstName(userDto.getFirstName());
        adminDto.setLastName(userDto.getLastName());
        adminDto.setEmail(userDto.getEmail());
        adminDto.setPhoneNumber(userDto.getPhoneNumber());
        if (userDto.getBalance() != null)
            adminDto.setBalance(userDto.getBalance());
        adminDto.setRoles(userDto.getRoles());
        return adminDto;
    }
    private UserDto convertAdminDtoToUserDtoAdd(AdminDto adminDto){
        UserDto userDto = new UserDto();
        userDto.setId(adminDto.getId());
        userDto.setUserName(adminDto.getUserName());
        userDto.setPassword(adminDto.getPassword());
        userDto.setFirstName(adminDto.getFirstName());
        userDto.setLastName(adminDto.getLastName());
        userDto.setEmail(adminDto.getEmail());
        userDto.setPhoneNumber(adminDto.getPhoneNumber());
        userDto.setBalance(adminDto.getBalance());
        return userDto;
    }
    private UserDto convertAdminDtoToUserDtoUpdate(AdminDto adminDto){
        UserDto userDto = new UserDto();
        userDto.setId(adminDto.getId());
        if (adminDto.getUserName() != null)
            userDto.setUserName(adminDto.getUserName());
        if (adminDto.getPassword() != null)
            userDto.setPassword(adminDto.getPassword());
        if (adminDto.getFirstName() != null)
            userDto.setFirstName(adminDto.getFirstName());
        if (adminDto.getLastName() != null)
            userDto.setLastName(adminDto.getLastName());
        if (adminDto.getEmail() != null)
            userDto.setEmail(adminDto.getEmail());
        if (adminDto.getPhoneNumber() != null)
            userDto.setPhoneNumber(adminDto.getPhoneNumber());
        if (adminDto.getBalance() != null)
            userDto.setBalance(adminDto.getBalance());
        if (adminDto.getRoles() != null)
            userDto.setRoles(adminDto.getRoles());
        return userDto;
    }
}
