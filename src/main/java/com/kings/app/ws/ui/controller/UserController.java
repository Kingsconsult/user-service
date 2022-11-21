package com.kings.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

import com.kings.app.ws.ui.model.response.OperationStatusModel;
import com.kings.app.ws.ui.model.response.RequestOperationStatus;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kings.app.ws.email.EmailDetails;
import com.kings.app.ws.exceptions.UserServiceException;
import com.kings.app.ws.service.EmailService;
import com.kings.app.ws.service.UserService;
import com.kings.app.ws.shared.dto.UserDto;
import com.kings.app.ws.ui.model.request.UserDetailsRequestModel;
import com.kings.app.ws.ui.model.response.ErrorMessages;
import com.kings.app.ws.ui.model.response.UserRest;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	private EmailService emailservice;


	@GetMapping(path="/{id}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE } )
	public UserRest getUser(@PathVariable String id)
	{
		UserRest returnValue = new UserRest();

		UserDto userDto = userService.getUserByUserId(id);

		BeanUtils.copyProperties(userDto, returnValue);

		return returnValue;
	}

	@GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value="page", defaultValue="1") int page,  @RequestParam(value="limit", defaultValue="10") int limit)
	{
		List<UserRest> returnValue = new ArrayList<>();

		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);

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

//		UserDto userDto = new UserDto();
//		BeanUtils.copyProperties(userDetails, userDto);

		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);

		UserDto createUser = userService.createUser(userDto);
		BeanUtils.copyProperties(createUser, returnValue);


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
}
