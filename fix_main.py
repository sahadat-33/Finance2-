with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import re

old_observer = """                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.lockApp()
                    }
                }"""

new_observer = """                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.lockApp()
                    } else if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.triggerFetchFromCloud()
                    }
                }"""

content = content.replace(old_observer, new_observer)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
