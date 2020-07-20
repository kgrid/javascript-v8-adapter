function process(inputs){
  // Use arkid, version, path
  var hello = context.getExecutor("hello-world/welcome")
  var bmi = context.getExecutor("v8-bmicalc-v1.0/bmicalc")

  var results ={}
  if(hello!=null) {
    results.message = hello.execute(JSON.stringify(inputs))
  }else {
    results.message = "Error: KO not found."
  }
  if(bmi!=null) {
    results.bmi =  bmi.execute(JSON.stringify(inputs))
  }else {
    results.bmi ="Error: KO not found."
  }
  return results;
}
