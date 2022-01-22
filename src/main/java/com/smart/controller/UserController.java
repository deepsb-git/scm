package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	//method for adding commom data for response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME "+ userName);
		
		//get the user using username(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER " + user);
		model.addAttribute("user", user);
		
	}
	
	//Dashboard Home
	@RequestMapping("/index")
    public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
    	return "normal/user_dashboard";
    }
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
		
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			
						
			
			// processing and uploading file..

						if (file.isEmpty()) {
							// if the file is empty then try our message
							System.out.println("File is empty");
							contact.setImage("contact.png");

						} else {
							// file the file to folder and update the name to contact
							contact.setImage(file.getOriginalFilename());

							File saveFile = new ClassPathResource("static/img").getFile();

							Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

							Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

							System.out.println("Image is uploaded");

						}
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			System.out.println("DATA " + contact);
			System.out.println("Added to database");
			
			//message success
			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));
	
		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			
			//message error
			session.setAttribute("message", new Message("Some went wrong !! Try again..", "danger"));

		}
		
		return "normal/add_contact_form";
	}
	
	//show contact handler
	//per page=[n]= 5
	//current page=[page]= 0
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		/*
		 * //we have to find contact through ContactRepository then pagination
		 * implmentation into it //this.contactRepository.findAll(); but we want
		 * specific contact which only logged in
		 * 
		 * String userName = principal.getName(); User user =
		 * this.userRepository.getUserByUserName(userName); List<Contact> contacts =
		 * user.getContacts();
		 */
		m.addAttribute("title","Show User Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//currentPage- page
		//contact per page - 3
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	
	// showing particular contact details.
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model,Principal principal) {
		System.out.println("CID " + cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//Unauthenticated user access denied have to code
		//1st get the userName through that user from repository
		//then get id
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		//sending data
		//model.addAttribute("contact", contact);
		
		//if matched then data send else not
   		  if (user.getId()==contact.getUser().getId()) {
			  model.addAttribute("contact",contact);
			  model.addAttribute("title",contact.getName());
		 
   		  }
		return "normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model,Principal principal,HttpSession session) {
		
		System.out.println("CID: " + cId);
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//checking assignment code write ???
		System.out.println("Contact "+contact.getcId());
		
		//contact.setUser(null);
		
		//we have to remove photo first before contact
		//image folder photo is there 
		//getting image from contact contact.getImage();
		
		//this.contactRepository.delete(contact);
		
		
		//current user
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		System.out.println("DELETED");
		session.setAttribute("message", new Message("contact deleted successfully !!!","success"));
		
		return "redirect:/user/show-contacts/0";
	} 
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		 m.addAttribute("title","Update Contact");
		 
		 Contact contact = this.contactRepository.findById(cid).get();
		 m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//update contact handler
	
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact , @RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
		
		try {
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			
			//image @ If he select image
			if (!file.isEmpty()) {
				
				//file work..
				//rewrite means old one delete new one add 
				
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				//update new photo
				
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				
				//new contact image set we have to take from oldcontactDetail
				contact.setImage(oldContactDetail.getImage());
			}
			
			//getting id to updateHandler
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated...","success"));
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME: " + contact.getName());
		System.out.println("CONTACT ID: " + contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	//creating order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal principal) throws RazorpayException {
		//System.out.println("Hey order function executed");
		System.out.println(data);
		
		int amt =Integer.parseInt(data.get("amount").toString());
		
		var client = new RazorpayClient("rzp_test_pbMQTFDRt6Yyp2","O5nPCBBUXGUOPpm7bk1GnMbP");
		
		JSONObject ob=new JSONObject();
		ob.put("amount",amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_555425");
		
		//creating new Order
		
		Order order = client.Orders.create(ob);
		System.out.println(order);
		
		//if you want you can save this to your data..
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
		
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data){
		
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myorder);
		
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
		
	}
}
