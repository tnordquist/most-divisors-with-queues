# most-divisors-with-queues
This program finds the number, within a specified range of integers from 1 to
whatever positive integer greater than one is chosen, with the most divisors.
The user can choose how many threads will divide the task. This program makes
use of a thread pool that can access a ConcurrentLinkedQueue for tasks and it
makes use of a LinkedBlockingQueue for threads to safely retrieve divisors
results for comparison in finding the most divisors and its number.
