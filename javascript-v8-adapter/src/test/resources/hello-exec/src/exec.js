function exec(user) {
    let executor = context.getExecutor("hello-world/welcome");
    let localUser = {name:"Bob"};
//    let localUserString = JSON.stringify(localUser);
    return executor.execute({...user});
}