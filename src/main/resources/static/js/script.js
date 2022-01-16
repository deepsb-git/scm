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
  