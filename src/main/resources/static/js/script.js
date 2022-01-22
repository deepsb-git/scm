console.log("this is script file");

/*old type function
function toggleSidebar() {
    
}*/

const toggleSidebar=()=>{
  if ($('.sidebar').is(":visible")){
     //true
      //if true close it

      $(".sidebar").css("display","none");
      $(".content").css("margin-left","0%");
  }else {
      //false
      //if false show it

      $(".sidebar").css("display","block");
      $(".content").css("margin-left","20%");
  }
};
  //first request to server to create order

       const paymentStart=()=>{
           console.log("payment started..");
           let amount=$("#payment_field").val();
           console.log(amount);
           if (amount == '' || amount == null){
               //alert("amount is required !!");
               swal("Failed !!", "amount is required !!", "error");
               return;
           }
           //code to sending request in server
           //we are using ajax-jquery to create order in server


           $.ajax(
               {
                 url:'/user/create_order',
                  // data:{amount:amount},(request won't go on the form of json
                   data:JSON.stringify({amount:amount,info:'order_request'}),
                   contentType:'application/json',
                   type:'POST',
                   dataType:'json',
                   success:function (response) {
                       //this callback function invoked when success triggered
                       console.log(response);
                       if(response.status== "created"){
                           //open payment form
                           let options={
                                key:'rzp_test_pbMQTFDRt6Yyp2',
                               amount:response.amount,
                               currency: "INR",
                               name: "Smart Contact Manager",
                               description: "Donation",
                               image:

                                   "https://www.freevector.com/uploads/vector/preview/3614/FreeVector-Circular-Logo.jpg",
                               order_id:response.id,
                               handler:function(response){
                                   console.log(response.razorpay_payment_id);
                                   console.log(response.razorpay_order_id);
                                   console.log(response.razorpay_signature);
                                   console.log("payment successful !!");

                                   updatePaymentOnServer(
                                       response.razorpay_payment_id,
                                       response.razorpay_order_id,
                                       "paid"
                                   );


                               },
                               prefill: {
                                   name: "",
                                   email: "",
                                   contact: "",
                               },
                               notes: {
                                   address: "Deeps B World !!! ",
                               },
                               theme: {
                                   color: "#3399cc",
                               },
                           };
                           let rzp=new Razorpay(options);

                           rzp.on("payment.failed", function (response) {
                               console.log(response.error.code);
                               console.log(response.error.description);
                               console.log(response.error.source);
                               console.log(response.error.step);
                               console.log(response.error.reason);
                               console.log(response.error.metadata.order_id);
                               console.log(response.error.metadata.payment_id);
                               //alert("Oops payment failed !!");
                               swal("Failed !!", "Oops payment failed !!", "error");

                           });

                           rzp.open()
                       }
                   },
                   error:function(error){
                     //invoked when error
                       console.log(error);
                       alert("something went wrong");
                   }
           }
           )

       }
      function   updatePaymentOnServer(payment_id,order_id,status)
       {
           $.ajax({
               url:'/user/update_order',
               // data:{amount:amount},(request won't go on the form of json
               data:JSON.stringify({payment_id: payment_id, order_id: order_id,status: status}),
               contentType:'application/json',
               type:'POST',
               dataType:'json',
               success:function(response){
                   swal("Good job!", "congrates !! Payment successful !!", "success");
               },
               error:function(error){
                   swal("Failed !!", " payment success !!", "not in server, contact u ASAP");

               },

           })

       }