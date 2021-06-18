function process(inputs){
  // Use arkid, version, path
  var hello = context.getExecutor("hello-world/welcome")
  var bmi = context.getExecutor("v8-bmicalc-v1.0/bmicalc")

  var results ={}
  if(hello!=null) {
    results.message = hello.execute(inputs, "application/json")
  }else {
    results.message = "Error: KO not found."
  }
  if(bmi!=null) {
    results.bmi =  bmi.execute(inputs, "application/json")
  }else {
    results.bmi ="Error: KO not found."
  }
  return results;
}
