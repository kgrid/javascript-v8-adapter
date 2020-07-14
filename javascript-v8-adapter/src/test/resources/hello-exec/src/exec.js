function exec(name) {
    let executor = context.getExecutor("hello-world/welcome");
    return executor.execute(name);
}