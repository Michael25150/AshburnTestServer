1) Открыть проект в intelij idea
2) gradle(меню справа) -> ktor -> double click buildFatJar
3) run main function from ServerSocketApplication.kt file
4) Если хотите подключить android studio эмулятор к сервеу, то нужно в проекте андроид в файле TCPClient.kt
   использовать SERVER_ADDRESS = "10.0.2.2"
