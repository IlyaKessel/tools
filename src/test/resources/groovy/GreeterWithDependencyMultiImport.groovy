import example.*

class Greeter {
    String sayHello() {
        def greet = new Dependency().message
        greet
    }
}

new Greeter().sayHello()