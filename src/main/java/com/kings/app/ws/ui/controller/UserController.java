package com.kings.app.ws.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


import com.kings.app.ws.ui.model.response.OperationStatusModel;
import com.kings.app.ws.ui.model.response.RequestOperationStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kings.app.ws.email.EmailDetails;
import com.kings.app.ws.exceptions.UserServiceException;
import com.kings.app.ws.service.AddressService;
import com.kings.app.ws.service.EmailService;
import com.kings.app.ws.service.UserService;
import com.kings.app.ws.shared.dto.AddressDTO;
import com.kings.app.ws.shared.dto.UserDto;
import com.kings.app.ws.ui.model.request.UserDetailsRequestModel;
import com.kings.app.ws.ui.model.response.AddressesRest;
import com.kings.app.ws.ui.model.response.ErrorMessages;
import com.kings.app.ws.ui.model.response.UserRest;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	EmailService emailservice;
	
	@Autowired
	AddressService addressService;
	
	@Autowired
	AddressService addressesService;

	@GetMapping(path="/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE } )
	public UserRest getUser(@PathVariable String id)
	{
		
		UserDto userDto = userService.getUserByUserId(id);
		UserRest returnValue = new UserRest();

		ModelMapper modelMapper = new ModelMapper();
		
		returnValue = modelMapper.map(userDto, UserRest.class);

		return returnValue;
	}

	@GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value="page", defaultValue="1") int page,  @RequestParam(value="limit", defaultValue="10") int limit)
	{
		List<UserRest> returnValue = new ArrayList<>();

		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			
			userModel = new ModelMapper().map(userDto, UserRest.class);

			returnValue.add(userModel);
		}
		return returnValue;
	}

	@PostMapping(
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
			)
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception
	{
		UserRest returnValue = new UserRest();

		if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);

		UserDto createUser = userService.createUser(userDto);

		returnValue = modelMapper.map(createUser, UserRest.class);

		return returnValue;
	}

    // Sending a simple Email
    @PostMapping("/sendMail")
    public String
    sendMail(@RequestBody EmailDetails details)
    {
        String status = emailservice.sendsimpleMail(details);

        return status;
    }

    // Sending a simple Email
    @PostMapping("/sendMailWithAttachment")
    public String sendMailWithAttachment(@RequestBody  EmailDetails details)
    {
        String status = emailservice.sendMailWithAttachment(details);

        return status;
    }

	@PutMapping(path="/{id}",
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails)
	{
		UserRest returnValue = new UserRest();

		if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updateUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updateUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path="/{id}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public OperationStatusModel DeleteUser(@PathVariable String id)
	{
		OperationStatusModel returnValue= new OperationStatusModel();

		returnValue.setOperationName(RequestOperationName.DELETE.name());
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

		userService.deleteUser(id);

		return  returnValue;
	}
	
	@GetMapping(path="/{userId}/addresses", 
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json" } )
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String  userId)
	{
		
		List<AddressesRest> addressesListRestModel = new ArrayList<>();
		
		List<AddressDTO> addressesDTO = addressesService.getAddresses(userId);
		
		
		if(addressesDTO != null && !addressesDTO.isEmpty()) {
			Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
			addressesListRestModel = new ModelMapper().map(addressesDTO, listType);
			
			for(AddressesRest addressRest: addressesListRestModel) {
				Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(userId, addressRest.getAddressId())).withSelfRel();
				addressRest.add(addressLink);
				
				Link userLink = linkTo(methodOn(UserController.class).getUser(userId)).withRel("user");
				addressRest.add(userLink);

			}
		}
				
		return CollectionModel.of(addressesListRestModel);

	}	
	
	@GetMapping(path="/{userId}/addresses/{addressId}", 
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json" } )
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId)
	{
		AddressDTO addressesDTO = addressService.getAddress(addressId);
		
		ModelMapper modelMapper = new ModelMapper();
		
		Link addressLink = linkTo(methodOn(UserController.class).getUserAddress(userId, addressId)).withSelfRel();
		Link addressesLink = linkTo(UserController.class).slash(userId).slash("addresses").withRel("addresses");

		
		Link userLink = linkTo(UserController.class).slash(userId).withRel("user");

		 AddressesRest addressRestModel = modelMapper.map(addressesDTO, AddressesRest.class);
		 
		 addressRestModel.add(addressLink);
		 addressRestModel.add(userLink);
		 addressRestModel.add(addressesLink);

		 
		return EntityModel.of(addressRestModel);

	}
}
