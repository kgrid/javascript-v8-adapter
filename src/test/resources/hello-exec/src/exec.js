function exec(user) {
    let executor = context.getExecutor("hello-world/welcome");

    return executor.execute(user);
}
