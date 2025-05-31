// WebSocket Client for Task Management App

let stompClient = null;
let isConnected = false;
let subscriptions = [];

/**
 * Connect to the WebSocket server
 */
function connect(onConnectCallback) {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function(frame) {
    console.log('Connected to WebSocket: ' + frame);
    isConnected = true;

    if (onConnectCallback && typeof onConnectCallback === 'function') {
      onConnectCallback();
    }
  }, function(error) {
    console.error('WebSocket connection error:', error);
    isConnected = false;
    // Try to reconnect after 5 seconds
    setTimeout(function() {
      connect(onConnectCallback);
    }, 5000);
  });
}

/**
 * Disconnect from the WebSocket server
 */
function disconnect() {
  if (stompClient !== null) {
    // Unsubscribe from all topics
    subscriptions.forEach(subscription => {
      if (subscription) {
        subscription.unsubscribe();
      }
    });
    subscriptions = [];

    stompClient.disconnect();
    console.log('Disconnected from WebSocket');
  }
  isConnected = false;
}

/**
 * Subscribe to all tasks updates
 */
function subscribeToAllTasks(callback) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  const subscription = stompClient.subscribe('/topic/tasks', function(message) {
    const event = JSON.parse(message.body);
    callback(event);
  });

  subscriptions.push(subscription);
  return true;
}

/**
 * Subscribe to specific task updates
 */
function subscribeToTask(taskId, callback) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  const subscription = stompClient.subscribe(`/topic/tasks/${taskId}`, function(message) {
    const event = JSON.parse(message.body);
    callback(event);
  });

  subscriptions.push(subscription);
  return true;
}

/**
 * Subscribe to all workspaces updates
 */
function subscribeToAllWorkspaces(callback) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  const subscription = stompClient.subscribe('/topic/workspaces', function(message) {
    const event = JSON.parse(message.body);
    callback(event);
  });

  subscriptions.push(subscription);
  return true;
}

/**
 * Subscribe to specific workspace updates
 */
function subscribeToWorkspace(workspaceId, callback) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  const subscription = stompClient.subscribe(`/topic/workspaces/${workspaceId}`, function(message) {
    const event = JSON.parse(message.body);
    callback(event);
  });

  subscriptions.push(subscription);
  return true;
}

/**
 * Subscribe to tasks in a specific workspace
 */
function subscribeToWorkspaceTasks(workspaceId, callback) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  const subscription = stompClient.subscribe(`/topic/workspace/${workspaceId}/tasks`, function(message) {
    const event = JSON.parse(message.body);
    callback(event);
  });

  subscriptions.push(subscription);
  return true;
}

/**
 * Send a task update through WebSocket
 */
function sendTaskUpdate(taskDTO) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  stompClient.send('/app/tasks.update', {}, JSON.stringify(taskDTO));
  return true;
}

/**
 * Send a task creation through WebSocket
 */
function sendTaskCreate(taskDTO) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  stompClient.send('/app/tasks.create', {}, JSON.stringify(taskDTO));
  return true;
}

/**
 * Send a task deletion through WebSocket
 */
function sendTaskDelete(taskDTO) {
  if (!isConnected) {
    console.error('Not connected to WebSocket server');
    return false;
  }

  stompClient.send('/app/tasks.delete', {}, JSON.stringify(taskDTO));
  return true;
}

// Export all functions for use in the application
window.websocketClient = {
  connect,
  disconnect,
  subscribeToAllTasks,
  subscribeToTask,
  subscribeToAllWorkspaces,
  subscribeToWorkspace,
  subscribeToWorkspaceTasks,
  sendTaskUpdate,
  sendTaskCreate,
  sendTaskDelete
};
