document.addEventListener("DOMContentLoaded", init)

function init() {
    const token = document.querySelector('meta[name="_csrf"]').content
    const tokenHeader = document.querySelector('meta[name="_csrf_header"]').content
    document
        .getElementById('notification-container')
        .addEventListener('click', e => closeNotification(tokenHeader, token, e))
    loadNotifications()
}

function loadNotifications() {
    fetch("/api/v1/notifications")
        .then(response => response.json())
        .then(addNotifications)
        .catch(error => console.error('Failed to load notifications: ' + error));
}

function closeNotification(tokenHeader, token, e) {
    if (e.target.classList.contains('btn-close')) {
        markAsRead(e.target.dataset.notificationId)
    }
}

function addNotifications(notifications) {
    if (notifications) {
        notifications.forEach(addNotification)
    }
}

function addNotification(notification) {
    let notificationContainer = document.getElementById('notification-container')
    if (notificationContainer && notificationContainer) {
        notificationContainer.prepend(createNotification(notification))
    }
}

function createNotification(notification) {
    let box = document.createElement('div')
    box.classList.add('notification-item', 'alert', 'alert-dismissible', 'fade', 'show')
    box.role = 'alert'
    box.textContent = notification.message
    let closeButton = document.createElement('button')
    closeButton.classList.add('btn-close')
    closeButton.type = 'button'
    closeButton.ariaLabel = 'Close'
    closeButton.dataset.bsDismiss = 'alert'
    closeButton.dataset.notificationId = notification.id
    box.appendChild(closeButton)
    switch (notification.type) {
        case 'CREDITED':
            box.classList.add('alert-success')
            break
        case 'WITHDRAWN':
            box.classList.add('alert-danger')
            break
        default: box.classList.add('alert-warning')
    }
    return box;
}
