const socket = new SockJS('/ws')
const stompClient = Stomp.over(socket)

document.addEventListener('DOMContentLoaded', connect)

function connect() {
    let header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content')
    let token = document.querySelector('meta[name="_csrf"]').getAttribute('content')
    stompClient.connect({[header]: token}, frame => {
        console.log(`Connected: ${frame}`)
        stompClient.subscribe('/user/queue/notifications', message => {
            addNotification(JSON.parse(message.body))
        })
    })
}

function markAsRead(notificationId) {
    let header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content')
    let token = document.querySelector('meta[name="_csrf"]').getAttribute('content')
    stompClient.send(`/app/notifications/${notificationId}/read`, {[header]: token}, JSON.stringify({}))
}
