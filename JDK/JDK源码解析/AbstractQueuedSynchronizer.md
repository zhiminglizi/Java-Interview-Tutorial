AbstractQueuedSynchronizer 抽象同步队列简称 AQS ，它是实现同步器的基础组件，
并发包中锁的底层就是使用 AQS 实现的.
大多数开发者可能永远不会直接使用AQS ，但是知道其原理对于架构设计还是很有帮助的,而且要理解ReentrantLock、CountDownLatch等高级锁我们必须搞懂 AQS.

# 1 整体感知
## 1.1 架构图
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMjA3XzIwMjAwMjA5MTk1MDI3NjQ4LnBuZw?x-oss-process=image/format,png)
AQS框架大致分为五层，自上而下由浅入深，从AQS对外暴露的API到底层基础数据.


当有自定义同步器接入时，只需重写第一层所需要的部分方法即可，不需要关注底层具体的实现流程。当自定义同步器进行加锁或者解锁操作时，先经过第一层的API进入AQS内部方法，然后经过第二层进行锁的获取，接着对于获取锁失败的流程，进入第三层和第四层的等待队列处理，而这些处理方式均依赖于第五层的基础数据提供层。

AQS 本身就是一套锁的框架，它定义了获得锁和释放锁的代码结构，所以如果要新建锁，只要继承 AQS，并实现相应方法即可。

## 1.2 类设计
该类提供了一种框架，用于实现依赖于先进先出（FIFO）等待队列的阻塞锁和相关的同步器（信号量，事件等）。此类的设计旨在为大多数依赖单个原子int值表示 state 的同步器提供切实有用的基础。子类必须定义更改此 state 的 protected 方法，并定义该 state 对于 acquired 或 released 此对象而言意味着什么。鉴于这些，此类中的其他方法将执行全局的排队和阻塞机制。子类可以维护其他状态字段，但是就同步而言，仅跟踪使用方法 *getState*，*setState* 和 *compareAndSetState* 操作的原子更新的int值。
子类应定义为用于实现其所在类的同步属性的非公共内部帮助器类。

子类应定义为用于实现其所在类的同步属性的非 public 内部辅助类。类AbstractQueuedSynchronizer不实现任何同步接口。 相反，它定义了诸如*acquireInterruptible*之类的方法，可以通过具体的锁和相关的同步器适当地调用这些方法来实现其 public 方法。

此类支持默认的排他模式和共享模式:
- 当以独占方式进行获取时，其他线程尝试进行的获取将无法成功
- 由多个线程获取的共享模式可能（但不一定）成功

该类不理解这些差异，只是从机制的意义上说，当共享模式获取成功时，下一个等待线程（如果存在）也必须确定它是否也可以获取。在不同模式下等待的线程们共享相同的FIFO队列。 通常，实现的子类仅支持这些模式之一，但也可以同时出现,比如在ReadWriteLock.仅支持排他模式或共享模式的子类无需定义支持未使用模式的方法.

此类定义了一个内嵌的 ConditionObject 类，可以由支持排他模式的子类用作Condition 的实现，该子类的 *isHeldExclusively* 方法报告相对于当前线程是否独占同步，使用当前 *getState* 值调用的方法 *release* 会完全释放此对象 ，并获得给定的此保存状态值，最终将该对象恢复为其先前的获取状态。否则，没有AbstractQueuedSynchronizer方***创建这样的条件，因此，如果无法满足此约束，请不要使用它。ConditionObject的行为当然取决于其同步器实现的语义。

此类提供了内部队列的检查，检测和监视方法，以及条件对象的类似方法。 可以根据需要使用 AQS 将它们导出到类中以实现其同步机制。

此类的序列化仅存储基础原子整数维护状态，因此反序列化的对象具有空线程队列。 需要序列化性的典型子类将定义一个readObject方法，该方法在反序列化时将其恢复为已知的初始状态。

# 2 用法
要将此类用作同步器的基础，使用*getState* *setState*和/或*compareAndSetState*检查和/或修改同步状态，以重新定义以下方法（如适用）
- tryAcquire
- tryRelease
- tryAcquireShared
- tryReleaseShared
- isHeldExclusively

默认情况下，这些方法中的每一个都会抛 *UnsupportedOperationException*。
这些方法的实现必须在内部是线程安全的，并且通常应简短且不阻塞。 定义这些方法是使用此类的**唯一**受支持的方法。 所有其他方法都被声明为final，因为它们不能独立变化。

从 AQS 继承的方法对跟踪拥有排他同步器的线程很有用。 鼓励使用它们-这将启用监视和诊断工具，以帮助用户确定哪些线程持有锁。

虽然此类基于内部的FIFO队列，它也不会自动执行FIFO获取策略。 独占同步的核心采用以下形式：
- Acquire
```java
while (!tryAcquire(arg)) {
     如果线程尚未入队，则将其加入队列；
     可能阻塞当前线程；
}
```
-  Release

```java
if (tryRelease(arg))
    取消阻塞第一个入队的线程;
```
共享模式与此相似，但可能涉及级联的signal。

acquire 中的检查是在入队前被调用，所以新获取的线程可能会在被阻塞和排队的其他线程之前插入。 但如果需要，可以定义tryAcquire、tryAcquireShared以通过内部调用一种或多种检查方法来禁用插入，从而提供公平的FIFO获取顺序。 特别是，如果*hasQueuedPredecessors*()(公平同步器专门设计的一种方法）返回true，则大多数公平同步器都可以定义tryAcquire返回false.

- 公平与否取决于如下代码：

```java
if (c == 0) {
    if (!hasQueuedPredecessors() &&
        compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(current);
        return true;
    }
}
```

对于默认的插入（也称为贪婪，放弃和convoey-avoidance）策略，吞吐量和可伸缩性通常最高。 尽管不能保证这是公平的或避免饥饿，但允许较早排队的线程在较晚排队的线程之前进行重新竞争，并且每个重新争用都有一次机会可以毫无偏向地成功竞争过进入的线程。 
同样，尽管获取通常无需自旋，但在阻塞前，它们可能会执行tryAcquire的多次调用，并插入其他任务。 如果仅短暂地保持排他同步，则这将带来自旋的大部分好处，而如果不进行排他同步，则不会带来很多负担。 如果需要的话，可以通过在调用之前使用“fast-path”检查来获取方法来增强此功能，并可能预先检查*hasContended*()和/或*hasQueuedThreads()*,以便仅在同步器可能不存在争用的情况下这样做。

此类为同步提供了有效且可扩展的基础，部分是通过将其使用范围规范化到可以依赖于int状态，acquire 和 release 参数以及内部的FIFO等待队列的同步器。 当这还不够时，可以使用原子类、自定义队列类和锁支持阻塞支持从较低级别构建同步器。

# 3 使用案例
这里是一个不可重入的排他锁，它使用值0表示解锁状态，使用值1表示锁定状态。虽然不可重入锁并不严格要求记录当前所有者线程，但是这个类这样做是为了更容易监视使用情况。它还支持条件，并暴露其中一个检测方法:

```java
 class Mutex implements Lock, java.io.Serializable {

   // 我们内部的辅助类
   private static class Sync extends AbstractQueuedSynchronizer {
     // 报告是否处于锁定状态
     protected boolean isHeldExclusively() {
       return getState() == 1;
     }

     // 如果 state 是 0,获取锁
     public boolean tryAcquire(int acquires) {
       assert acquires == 1; // Otherwise unused
       if (compareAndSetState(0, 1)) {
         setExclusiveOwnerThread(Thread.currentThread());
         return true;
       }
       return false;
     }

     // 通过将 state 置 0 来释放锁
     protected boolean tryRelease(int releases) {
       assert releases == 1; // Otherwise unused
       if (getState() == 0) throw new IllegalMonitorStateException();
       setExclusiveOwnerThread(null);
       setState(0);
       return true;
     }

     //  提供一个 Condition
     Condition newCondition() { return new ConditionObject(); }

     // 反序列化属性
     private void readObject(ObjectInputStream s)
         throws IOException, ClassNotFoundException {
       s.defaultReadObject();
       setState(0); // 重置到解锁状态
     }
   }

   // 同步对象完成所有的工作。我们只是期待它.
   private final Sync sync = new Sync();

   public void lock()                { sync.acquire(1); }
   public boolean tryLock()          { return sync.tryAcquire(1); }
   public void unlock()              { sync.release(1); }
   public Condition newCondition()   { return sync.newCondition(); }
   public boolean isLocked()         { return sync.isHeldExclusively(); }
   public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
   public void lockInterruptibly() throws InterruptedException {
     sync.acquireInterruptibly(1);
   }
   public boolean tryLock(long timeout, TimeUnit unit)
       throws InterruptedException {
     return sync.tryAcquireNanos(1, unit.toNanos(timeout));
   }
 }
```

这是一个闩锁类，它类似于*CountDownLatch*，只是它只需要一个单信号就可以触发。因为锁存器是非独占的，所以它使用共享的获取和释放方法。

```java
 class BooleanLatch {

   private static class Sync extends AbstractQueuedSynchronizer {
     boolean isSignalled() { return getState() != 0; }

     protected int tryAcquireShared(int ignore) {
       return isSignalled() ? 1 : -1;
     }

     protected boolean tryReleaseShared(int ignore) {
       setState(1);
       return true;
     }
   }

   private final Sync sync = new Sync();
   public boolean isSignalled() { return sync.isSignalled(); }
   public void signal()         { sync.releaseShared(1); }
   public void await() throws InterruptedException {
     sync.acquireSharedInterruptibly(1);
   }
 }
```

# 4 基本属性与框架
## 4.1 继承体系图
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTU4XzIwMjAwMjEwMjMyNTQwMzUwLnBuZw?x-oss-process=image/format,png)
## 4.2 定义
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDk2XzIwMjAwMjEwMjMxMTIwMTMzLnBuZw?x-oss-process=image/format,png)

可知 AQS 是一个抽象类，生来就是被各种子类锁继承的。继承自AbstractOwnableSynchronizer，其作用就是为了知道当前是哪个线程获得了锁，便于后续的监控
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDYyXzIwMjAwMjEwMjMyMzE4MzcyLnBuZw?x-oss-process=image/format,png)


## 4.3 属性
### 4.3.1 状态信息
- volatile 修饰，对于可重入锁，每次获得锁 +1，释放锁 -1
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMTEwXzIwMjAwMjEwMjMyOTI0MTczLnBuZw?x-oss-process=image/format,png)
- 可以通过 *getState* 得到同步状态的当前值。该操作具有 volatile 读的内存语义。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTM5XzIwMjAwMjEwMjMzMjM0OTE0LnBuZw?x-oss-process=image/format,png)
- setState 设置同步状态的值。该操作具有 volatile 写的内存语义
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDc4XzIwMjAwMjEwMjMzOTI2NjQ3LnBuZw?x-oss-process=image/format,png)
- compareAndSetState 如果当前状态值等于期望值，则以原子方式将同步状态设置为给定的更新值。此操作具有 volatile 读和写的内存语义
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDMyXzIwMjAwMjEwMjM1MjM1NDAzLnBuZw?x-oss-process=image/format,png)
- 自旋比使用定时挂起更快。粗略估计足以在非常短的超时时间内提高响应能力，当设置等待时间时才会用到这个属性
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDY0XzIwMjAwMjExMDAzOTIzNjQxLnBuZw?x-oss-process=image/format,png)

这写方法都是Final的，子类无法重写。
- 独占模式
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODIzXzIwMjAwMjExMDIyNTI0NjM5LnBuZw?x-oss-process=image/format,png)
- 共享模式
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDQzXzIwMjAwMjExMDIyNTUxODMwLnBuZw?x-oss-process=image/format,png)
### 4.3.2  同步队列
![](https://img-blog.csdnimg.cn/2020100800492945.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNTg5NTEw,size_16,color_FFFFFF,t_70#pic_center)
- 作用
阻塞获取不到锁(独占锁)的线程，并在适当时机从队首释放这些线程。

同步队列底层数据结构是个双向链表。

- 等待队列的头，延迟初始化。 除初始化外，只能通过 *setHead* 方法修改
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODc4XzIwMjAwMjExMDIzMjU4OTU4LnBuZw?x-oss-process=image/format,png)
注意：如果head存在，则其waitStatus保证不会是 *CANCELLED*

- 等待队列的尾部，延迟初始化。 仅通过方法 *enq* 修改以添加新的等待节点
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDk0XzIwMjAwMjExMDIzNTU3MTkyLnBuZw?x-oss-process=image/format,png)

### 4.3.4 条件队列
#### 为什么需要条件队列
同步队列并不是所有场景都能搞定，在遇到锁 + 队列结合的场景时，就需要 Lock + Condition，先使用 Lock 决定
- 哪些线程可以获得锁
- 哪些线程需要到同步队列里面排队阻塞

获得锁的多个线程在碰到队列满或空时，可以使用 Condition 来管理这些线程，让这些线程阻塞等待，然后在合适的时机后，被正常唤醒。

同步队列 + 条件队列联手使用的场景，最多被使用到锁 + 队列的场景中。

#### 作用
AQS 的内部类，结合锁实现线程同步。存放调用条件变量的 await 方法后被阻塞的线程

- 实现了 Condition 接口，而 Condition 接口就相当于 Object 的各种监控方法
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMTEwXzIwMjAwMjExMDI0MDUzNi5wbmc?x-oss-process=image/format,png)
需要使用时，直接 new ConditionObject()。

### 4.3.5 Node
同步队列和条件队列的共用节点。
入队时，用 Node 把线程包装一下，然后把 Node 放入两个队列中，我们看下 Node 的数据结构，如下：
####  4.3.5.1 模式
- 共享模式
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDUyXzIwMjAwMjExMDI1NTQxMTYyLnBuZw?x-oss-process=image/format,png)
- 独占模式
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTAzXzIwMjAwMjExMDI1NjE2NzkyLnBuZw?x-oss-process=image/format,png)

####  4.3.5.2  等待状态
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDUzXzIwMjAwMjExMTk0NjU4MjA5LnBuZw?x-oss-process=image/format,png)
注意等待状态仅能为如下值
##### SIGNAL
- 同步队列中的节点在自旋获取锁时，如果前一个节点的状态是 `SIGNAL`，那么自己就直接被阻塞，否则会一直自旋
- 该节点的后继节点会被（或很快）阻塞（通过park），因此当前节点释放或取消时必须unpark其后继节点。为了避免竞争，acquire方法必须首先指示它们需要一个 signal，然后重试原子获取，然后在失败时阻塞。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTEwXzIwMjAwMjExMTgyMTQwODEwLnBuZw?x-oss-process=image/format,png)
##### CANCELLED
- 指示线程已被取消![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDM1XzIwMjAwMjExMDI1ODQyNDQ3LnBuZw?x-oss-process=image/format,png)
取消：由于超时或中断，该节点被取消。
节点永远不会离开此状态，即此为一种终极状态。特别是，具有 cancelled 节点的线程永远不会再次阻塞。



##### CONDITION
该节点当前在条件队列中，当节点从同步队列被转移到条件队列时，状态就会被更改成 `CONDITION`![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODY0XzIwMjAwMjExMTgyMTE0MjYzLnBuZw?x-oss-process=image/format,png)
在被转移之前，它不会用作同步队列的节点，此时状态将设置为0（该值的使用与该字段的其他用途无关，仅仅是简化了机制）。

##### PROPAGATE
线程处在 `SHARED` 情景下，该字段才会启用。

-  指示下一个acquireShared应该无条件传播，共享模式下，该状态的进程处于 Runnable 状态![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDgxXzIwMjAwMjExMTk0MzM2MzMxLnBuZw?x-oss-process=image/format,png)
*releaseShared* 应该传播到其他节点。 在*doReleaseShared*中对此进行了设置（仅适用于头节点），以确保传播继续进行，即使此后进行了其他操作也是如此。
##### 0
以上都不是，初始化状态。
##### 小结
这些值是以数字方式排列，极大方便了开发者的使用。我们在平时开发也可以定义一些有特殊意义的常量值。

非负值表示节点不需要 signal。 因此，大多数代码并不需要检查特定值，检查符号即可。

- 对于普通的同步节点，该字段初始化为0
- 对于条件节点，该字段初始化为`CONDITION`

使用CAS（或在可能的情况下进行无条件的 volatile 写）对其进行修改。

注意两个状态的区别
- state 是锁的状态，int 型，子类继承 AQS 时，都是要根据 state 字段来判断有无得到锁
- waitStatus 是节点（Node）的状态

#### 4.3.5.3 数据结构
##### 前驱节点
- 链接到当前节点/线程所依赖的用来检查 *waitStatus* 的前驱节点
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODY0XzIwMjAwMjEyMDMyNjI1NjYxLnBuZw?x-oss-process=image/format,png)

在入队期间赋值，并且仅在出队时将其清空（为了GC）。

此外，在取消一个前驱结点后，在找到一个未取消的节点后会短路，这将始终存在，因为头节点永远不会被取消：只有成功 acquire 后，一个节点才会变为头。

取消的线程永远不会成功获取，并且线程只会取消自身，不会取消任何其他节点。

##### 后继节点

链接到后继节点，当前节点/线程在释放时将其unpark。 在入队时赋值，在绕过已取消的前驱节点时进行调整，在出队时清零（为了GC）。 入队操作直到附加后才赋值前驱节点的下一个字段，因此看到 null 的下一个字段并不一定意味着该节点位于队列末尾。 但是，如果下一个字段显示为空，则我们可以从尾部扫描上一个以进行再次检查。 已取消节点的下一个字段设置为指向节点本身而不是null，以使isOnSyncQueue的工作更轻松。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDg3XzIwMjAwMjEyMDM1NTAzNjAyLnBuZw?x-oss-process=image/format,png)
- 使该节点入队的线程。 在构造时初始化，使用后消亡。![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODcwXzIwMjAwMjEyMjAwMTI3OTkwLnBuZw?x-oss-process=image/format,png)

在同步队列中，nextWaiter 表示当前节点是独占模式还是共享模式
在条件队列中，nextWaiter 表示下一个节点元素

链接到在条件队列等待的下一个节点，或者链接到特殊值`SHARED`。 由于条件队列仅在以独占模式保存时才被访问，因此我们只需要一个简单的链接队列即可在节点等待条件时保存节点。 然后将它们转移到队列中以重新获取。 并且由于条件只能是独占的，因此我们使用特殊值来表示共享模式来保存字段。![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDUxXzIwMjAwMjEyMjAxMjMyODMucG5n?x-oss-process=image/format,png)
# 5 Condition 接口
JDK5 时提供。

- 条件队列 ConditionObject 实现了 Condition 接口
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDc1XzIwMjAwMjEyMjA0NjMxMTMzLnBuZw?x-oss-process=image/format,png)
- 本节就让我们一起来研究之
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDgwXzIwMjAwMjEyMjA1MzQ2NzIyLnBuZw?x-oss-process=image/format,png)

Condition 将对象监视方法（wait，notify和notifyAll）分解为不同的对象，从而通过与任意Lock实现结合使用，从而使每个对象具有多个wait-sets。 当 Lock 替换了 synchronized 方法和语句的使用，Condition 就可以替换了Object监视器方法的使用。

Condition 的实现可以提供与 Object 监视方法不同的行为和语义，例如保证通知的顺序，或者在执行通知时不需要保持锁定。 如果实现提供了这种专门的语义，则实现必须记录这些语义。

请注意，Condition实例只是普通对象，它们本身可以用作 synchronized 语句中的目标，并且可以调用自己的监视器 wait 和 notification 方法。 获取 Condition 实例的监视器锁或使用其监视器方法与获取与该条件相关联的锁或使用其 await 和 signal 方法没有特定的关系。 建议避免混淆，除非可能在自己的实现中，否则不要以这种方式使用 Condition 实例。

```java
 class BoundedBuffer {
   final Lock lock = new ReentrantLock();
   final Condition notFull  = lock.newCondition(); 
   final Condition notEmpty = lock.newCondition(); 

   final Object[] items = new Object[100];
   int putptr, takeptr, count;

   public void put(Object x) throws InterruptedException {
     lock.lock();
     try {
       while (count == items.length)
         notFull.await();
       items[putptr] = x;
       if (++putptr == items.length) putptr = 0;
       ++count;
       notEmpty.signal();
     } finally {
       lock.unlock();
     }
   }

   public Object take() throws InterruptedException {
     lock.lock();
     try {
       while (count == 0)
         notEmpty.await();
       Object x = items[takeptr];
       if (++takeptr == items.length) takeptr = 0;
       --count;
       notFull.signal();
       return x;
     } finally {
       lock.unlock();
     }
   }
 }
```
（ArrayBlockingQueue类提供了此功能，因此没有理由实现此示例用法类。）
定义出一些方法，这些方法奠定了条件队列的基础
## API
### await
- 使当前线程等待，直到被 signalled 或被中断
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTkwXzIwMjAwMjEyMjExNzU3ODYyLnBuZw?x-oss-process=image/format,png)

与此 Condition 相关联的锁被原子释放，并且出于线程调度目的，当前线程被禁用，并且处于休眠状态，直到发生以下四种情况之一：
- 其它线程为此 Condition 调用了 signal 方法，并且当前线程恰好被选择为要唤醒的线程
- 其它线程为此 Condition 调用了 signalAll 方法
- 其它线程中断了当前线程，并且当前线程支持被中断
- 要么发生“虚假唤醒”。

在所有情况下，在此方法可以返回之前，必须重新获取与此 Condition 关联的锁，才能真正被唤醒。当线程返回时，可以保证保持此锁。

### await 超时时间
- 使当前线程等待，直到被 signal 或中断，或经过指定的等待时间
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDczXzIwMjAwMjEyMjIxMjU3NTcyLnBuZw?x-oss-process=image/format,png)

此方法在行为上等效于：
 

```java
awaitNanos(unit.toNanos(time)) > 0
```
所以，虽然入参可以是任意单位的时间，但其实仍会转化成纳秒
### awaitNanos
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTg0XzIwMjAwMjEyMjIxOTMxNTU5LnBuZw?x-oss-process=image/format,png)
注意这里选择纳秒是为了避免计算剩余等待时间时的截断误差


### signal()
- 唤醒条件队列中的一个线程，在被唤醒前必须先获得锁
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDYzXzIwMjAwMjEyMjMwNTQ3NjMzLnBuZw?x-oss-process=image/format,png)
### signalAll()
- 唤醒条件队列中的所有线程
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODkyXzIwMjAwMjEyMjMwNjUzNTM5LnBuZw?x-oss-process=image/format,png)
# 6 锁的获取
获取锁显式的方法就是 Lock.lock () ，最终目的其实是想让线程获得对资源的访问权。而 Lock 又是 AQS 的子类，lock 方法根据情况一般会选择调用 AQS 的 acquire 或 tryAcquire 方法。

acquire 方法 AQS 已经实现了，tryAcquire 方法是等待子类去实现，acquire 方法制定了获取锁的框架，先尝试使用 tryAcquire 方法获取锁，获取不到时，再入同步队列中等待锁。tryAcquire 方法 AQS 中直接抛出一个异常，表明需要子类去实现，子类可以根据同步器的 state 状态来决定是否能够获得锁，接下来我们详细看下 acquire 的源码解析。

acquire 也分两种，一种是独占锁，一种是共享锁

## 6.1 acquire 独占锁
- 独占模式下，尝试获得锁
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMjQyXzIwMjAwMjEzMDAwODUzODE5LnBuZw?x-oss-process=image/format,png)

在独占模式下获取，忽略中断。 通过至少调用一次 *tryAcquire(int)* 来实现，并在成功后返回。 否则，将线程排队，并可能反复阻塞和解除阻塞，并调用 *tryAcquire(int)*  直到成功。 该方法可用于实现方法 Lock.lock()。
对于 arg 参数，该值会传送给 *tryAcquire*，但不会被解释，可以实现你喜欢的任何内容。

- 看一下 *tryAcquire* 方法
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMDAzXzIwMjAwMjEzMDAyMDEwNTM2LnBuZw?x-oss-process=image/format,png)AQS 对其只是简单的实现，具体获取锁的实现方法还是由各自的公平锁和非公平锁单独实现，实现思路一般都是 CAS 赋值 state  来决定是否能获得锁（阅读后文的 ReentrantLock 核心源码解析即可）。

### 执行流程
1. 尝试执行一次 tryAcquire
- 成功直接返回
- 失败走 2

2. 线程尝试进入同步队列，首先调用 addWaiter 方法，把当前线程放到同步队列的队尾

3. 接着调用 acquireQueued 方法
- 阻塞当前节点
- 节点被唤醒时，使其能够获得锁

4. 如果 2、3 失败了，中断线程

### 6.1.1 addWaiter
将当前线程放入等待队列
```java
private Node addWaiter(Node mode) {
	// 创建一个等待节点代表当前线程
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    // 添加到等待队列
    enq(node);
    return node;
}
```

#### 执行流程
1. 通过当前的线程和锁模式新建一个节点
2. pred 指针指向尾节点tail
3. 将Node 的 prev 指针指向 pred
4. 通过compareAndSetTail方法，完成尾节点的设置。该方法主要是对tailOffset和Expect进行比较，如果tailOffset的Node和Expect的Node地址是相同的，那么设置Tail的值为Update的值。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyOTkzXzIwMjAwMjEzMDEwNjU3Mjk0LnBuZw?x-oss-process=image/format,png)

- 如果 pred 指针为 null(说明等待队列中没有元素)，或者当前 pred 指针和 tail 指向的位置不同（说明被别的线程已经修改），就需要 enq

```java
private Node enq(final Node node) {
	// juc 中看到死循环，肯定有多个分支
    for (;;) {
    	// 初始值为 null
        Node t = tail;
        if (t == null) { // 那就初始化
        	// 由于是多线程操作，为保证只有一个
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
        	// 将当前线程 node 的 prev 设为t
        	// 注意这里先更新的是 prev 指针
            node.prev = t;
            if (compareAndSetTail(t, node)) {
            	// 有 next延后更新的，所以通过 next 不一定找得到后续结点，所以释放锁时是从 tail 节点开始找 prev 指针
                t.next = node;
                return t;
            }
            // 因为prev 指针是 volatile 的，所以这里的 node.prev = t 线程是可见的。所以只要 compareAndSetTail，那么必然其他线程可以通过 c 节点的 prev 指针访问前一个节点且可见。
        }
    }
}
```
if 分支
![](https://img-blog.csdnimg.cn/20210410211928179.png)
else 分支
![](https://img-blog.csdnimg.cn/20210410212011713.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNTg5NTEw,size_16,color_FFFFFF,t_70)


把新的节点添加到同步队列的队尾。

如果没有被初始化，需要进行初始化一个头结点出来。但请注意，初始化的头结点并不是当前线程节点，而是调用了无参构造函数的节点。如果经历了初始化或者并发导致队列中有元素，则与之前的方法相同。其实，addWaiter就是一个在双端链表添加尾节点的操作，需要注意的是，双端链表的头结点是一个无参构造函数的头结点。

线程获取锁的时候，过程大体如下：
1. 当没有线程获取到锁时，线程1获取锁成功
2. 线程2申请锁，但是锁被线程1占有
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkyODcyXzIwMjAwMjEzMDIwNzQ4MzAzLnBuZw?x-oss-process=image/format,png)
如果再有线程要获取锁，依次在队列中往后排队即可。

在 addWaiter 方法中，并没有进入方法后立马就自旋，而是先尝试一次追加到队尾，如果失败才自旋，因为大部分操作可能一次就会成功，这种思路在自己写自旋的时候可以多多参考哦。

### 6.1.2 acquireQueued
此时线程节点 node已经通过 addwaiter 放入了等待队列，考虑是否让线程去等待。

阻塞当前线程。

- 自旋使前驱结点的 waitStatus 变成 `signal`，然后阻塞自身
- 获得锁的线程执行完成后，释放锁时，会唤醒阻塞的节点，之后再自旋尝试获得锁

```java
final boolean acquireQueued(final Node node, int arg) {
	// 标识是否成功取得资源
    boolean failed = true;
    try {
        // 标识是否在等待过程被中断过
        boolean interrupted = false;
        // 自旋，结果要么获取锁或者中断
        for (;;) {
        	// 获取等待队列中的当前节点的前驱节点
            final Node p = node.predecessor();
            // 代码优化点：若 p 是头结点，说明当前节点在真实数据队列的首部，就尝试获取锁（此前的头结点还只是虚节点）
            if (p == head && tryAcquire(arg)) {
            	// 获取锁成功，将头指针移动到当前的 node
                setHead(node);
                p.next = null; // 辅助GC
                failed = false;
                return interrupted;
            }
            // 获取锁失败了，走到这里
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```
看其中的具体方法：
#### setHead
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNTMzMTkzMTQwXzIwMjAwMjEzMDI0MTUzMjYzLnBuZw?x-oss-process=image/format,png)

方法的核心：
#### shouldParkAfterFailedAcquire
依据前驱节点的等待状态判断当前线程是否应该被阻塞
```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
	// 获取头结点的节点状态
    int ws = pred.waitStatus;
    // 说明头结点处于唤醒状态
    if (ws == Node.SIGNAL)
        /*
         * 该节点已经设置了状态，要求 release 以 signal，以便可以安全park
         */
        return true;
    // 前文说过 waitStatus>0 是取消状态    
    if (ws > 0) {
        /*
         * 跳过已被取消的前驱结点并重试
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        /*
         * waitStatus 必须为 0 或 PROPAGATE。 表示我们需要一个 signal，但不要 park。 调用者将需要重试以确保在 park 之前还无法获取。
         */
        // 设置前驱节点等待状态为 SIGNAL 
        // 给头结点放一个信物，告诉此时
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

为避免自旋导致过度消费 CPU 资源，以判断前驱节点的状态来决定是否挂起当前线程
- 挂起流程图
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzNzA2XzIwMjAwMjEzMDQ0MzU1NjI2LnBuZw?x-oss-process=image/format,png)

如下处理 prev 指针的代码。shouldParkAfterFailedAcquire 是获取锁失败的情况下才会执行，进入该方法后，说明共享资源已被获取，当前节点之前的节点都不会出现变化，因此这个时候变更 prev 指针较安全。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTY0MTA3XzIwMjAwMjEzMDUyODA4NTc5LnBuZw?x-oss-process=image/format,png)
#### parkAndCheckInterrupt
- 将当前线程挂起，阻塞调用栈并返回当前线程的中断状态

```java
private final boolean parkAndCheckInterrupt() {
	// 进入休息区 unpark就是从休息区唤醒
    LockSupport.park(this);
    return Thread.interrupted();
}
```

- 一图小结该方法流程
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTY0MDUwXzIwMjAwMjEzMDQ0MDM0OTI1LnBuZw?x-oss-process=image/format,png)
从上图可以看出，跳出当前循环的条件是当“前驱节点是头结点，且当前线程获取锁成功”。

### 6.1.3 cancelAcquire
shouldParkAfterFailedAcquire中取消节点是怎么生成的呢？什么时候会把一个节点的waitStatus设置为-1？又是在什么时间释放节点通知到被挂起的线程呢？
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzODc1XzIwMjAwMjEzMDQ1NTA5NzE2LnBuZw?x-oss-process=image/format,png)

```java
    private void cancelAcquire(Node node) {
        // 如果节点不存在，无视该方法
        if (node == null)
            return;
		// 设置该节点不关联任何线程，即虚节点
        node.thread = null;

        // 跳过被取消的前驱结点们
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;


        // predNext 是要取消拼接的明显节点。如果没有，以下情况 CAS 将失败，在这种情况下，我们输掉了和另一个cancel或signal的竞争，因此无需采取进一步措施。
        // 通过前驱节点，跳过取消状态的node
        Node predNext = pred.next;

        // 这里可以使用无条件写代替CAS，把当前node的状态设置为CANCELLED
        // 在这个原子步骤之后，其他节点可以跳过我们。
        // 在此之前，我们不受其他线程的干扰。
        node.waitStatus = Node.CANCELLED;

        // 如果是 tail 节点, 移除自身
        // 如果当前节点是尾节点，将从后往前的第一个非取消状态的节点设置为尾节点
  // 更新失败的话，则进入else，如果更新成功，将tail的后继节点设置为null
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // 辅助 GC
        }
    }
```
当前的流程：
- 获取当前节点的前驱节点，如果前驱节点的状态是`CANCELLED`，那就一直往前遍历，找到第一个`waitStatus <= 0`的节点，将找到的Pred节点和当前Node关联，将当前Node设置为`CANCELLED`。
- 根据当前节点的位置，考虑以下三种情况：
(1) 当前节点是尾节点。
(2) 当前节点是Head的后继节点。
(3) 当前节点不是Head的后继节点，也不是尾节点。

根据(2)，来分析每一种情况的流程。

- 当前节点是尾节点
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzODczXzIwMjAwMjEzMDUyNTQzNjczLnBuZw?x-oss-process=image/format,png)
- 当前节点是Head的后继节点
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzNjg5XzIwMjAwMjEzMDUyNjEyMTA0LnBuZw?x-oss-process=image/format,png)
- 当前节点不是Head的后继节点，也不是尾节点
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzODQ2XzIwMjAwMjEzMDUyNjMzMTIyLnBuZw?x-oss-process=image/format,png)
通过上面的流程，我们对于`CANCELLED`节点状态的产生和变化已经有了大致的了解，但是为什么所有的变化都是对Next指针进行了操作，而没有对Prev指针进行操作呢？什么情况下会对Prev指针进行操作？

> 执行 `cancelAcquire` 时，当前节点的前驱节点可能已经出队（已经执行过try代码块中的shouldParkAfterFailedAcquire），如果此时修改 prev 指针，有可能会导致 prev 指向另一个已经出队的 Node，因此这块变化 prev 指针不安全。

## 6.2 tryAcquireNanos
尝试以独占模式获取，如果中断将中止，如果超过给定超时将直接失败。首先检查中断状态，然后至少调用一次#tryAcquire，成功后返回。否则，线程将排队，可能会反复地阻塞和取消阻塞，调用#tryAcquire，直到成功或线程中断或超时结束。此方法可用于实现方法 Lock#tryLock(long, TimeUnit)。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTUvNTA4ODc1NV8xNTgxNzA4NjM2MjQ3XzIwMjAwMjE1MDI0MTU0Njg1LnBuZw?x-oss-process=image/format,png)

尝试性的获取锁, 获取锁不成功, 直接加入到同步队列，加入操作即在*doAcquireNanos*
### doAcquireNanos
以独占限时模式获取。
```java
private boolean doAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    if (nanosTimeout <= 0L)
        return false;
    // 截止时间    
    final long deadline = System.nanoTime() + nanosTimeout;
    // 将当前的线程封装成 Node 加入到同步对列里面
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        for (;;) {
        	// 获取当前节点的前驱节点(当一个n在同步对列里, 并且没有获取
        	// lock 的 node 的前驱节点不可能是 null)
            final Node p = node.predecessor();
            // 判断前驱节点是否为 head
            // 前驱节点是 head, 存在两种情况 
            //	(1) 前驱节点现在持有锁 
            //	(2) 前驱节点为 null, 已经释放锁, node 现在可以获取锁
            // 则再调用 tryAcquire 尝试获取
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // 辅助GC
                failed = false;
                return true;
            }
            // 计算剩余时间
            nanosTimeout = deadline - System.nanoTime();
            // 超时，直接返回 false
            if (nanosTimeout <= 0L)
                return false;
            // 调用 shouldParkAfterFailedAcquire 判断是否需要阻塞
            if (shouldParkAfterFailedAcquire(p, node) &&
            	// 若未超时, 并且大于 spinForTimeoutThreshold, 则将线程挂起
                nanosTimeout > spinForTimeoutThreshold)
                LockSupport.parkNanos(this, nanosTimeout);
            if (Thread.interrupted())
                throw new InterruptedException();
        }
    } finally {
    	// 在整个获取中出错(中断/超时等)，则清除该节点
        if (failed)
            cancelAcquire(node);
    }
}
```
## 6.3 acquireSharedInterruptibly
- 以共享模式获取，如果中断将中止。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTYvNTA4ODc1NV8xNTgxNzkwODI3NzczXzIwMjAwMjE2MDA1MDA3MTcyLnBuZw?x-oss-process=image/format,png)
首先检查中断状态，然后至少调用一次  tryAcquireShared(int)，成功后返回。否则，线程将排队，可能会反复阻塞和取消阻塞，调用 tryAcquireShared(int)，直到成功或线程被中断。

arg 参数，这个值被传递给 tryAcquireShared(int)，但未被解释，可以代表你喜欢的任何东西。如果当前线程被中断，则抛 InterruptedException。

### doAcquireSharedInterruptibly
共享可中断模式的获取锁
```java
private void doAcquireSharedInterruptibly(int arg)
    throws InterruptedException {
    // 创建"当前线程"的 Node 节点，且其中记录的共享锁
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        for (;;) {
        	// 获取前驱节点
            final Node p = node.predecessor();
            // 如果前驱节点是头节点
            if (p == head) {
            	// 尝试获取锁(由于前驱节点为头节点，所以可能此时前驱节点已经成功获取了锁，所以尝试获取一下)
                int r = tryAcquireShared(arg);
                // 获取锁成功
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // 辅助 GC
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```
## tryAcquireShared
```java
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    // step1：若存在写锁 && 写锁不是当前线程，则直接排队（这里也能表明写锁优先级更高）
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;
    int r = sharedCount(c);
    // step2：读锁是否该阻塞，对于非公平模式下写锁获取优先级会高，如果存在要获取写锁的线程则读锁需要让步，公平模式下则先来先到
    if (!readerShouldBlock() && 
        // 读锁使用高16位，因此存在获取上限为2^16-1
        r < MAX_COUNT &&
        // step3：CAS修改读锁状态，实际上是读锁状态+1
        compareAndSetState(c, c + SHARED_UNIT)) {
        // step4：执行到这里说明读锁已经获取成功，因此需要记录线程状态。
        if (r == 0) {
            firstReader = current; // firstReader是把读锁状态从0变成1的那个线程
            firstReaderHoldCount = 1;
        } else if (firstReader == current) { 
            firstReaderHoldCount++;
        } else {
            // 这些代码实际上是从ThreadLocal中获取当前线程重入读锁的次数，然后自增下。
            HoldCounter rh = cachedHoldCounter; // cachedHoldCounter是上一个获取锁成功的线程
            if (rh == null || rh.tid != getThreadId(current))
                cachedHoldCounter = rh = readHolds.get();
            else if (rh.count == 0)
                readHolds.set(rh);
            rh.count++;
        }
        return 1;
    }
    // 当操作2，操作3失败时执行该逻辑
    return fullTryAcquireShared(current);
}
```

# 7 锁的释放
## 7.1 release
以独占模式释放。 如果 tryRelease 返回true，则通过解锁一个或多个线程来实现。此方法可用于实现方法 Lock#unlock 

arg 参数将传送到 tryRelease，并且可以表示你自己喜欢的任何内容。
- 自定义实现的 *tryRelease* 如果返回 *true*，说明该锁没有被任何线程持有
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTY0MDcyXzIwMjAwMjEzMDU0MzAzNTg4LnBuZw?x-oss-process=image/format,png)
- 头结点不为空并且头结点的waitStatus不是初始化节点情况，解除线程挂起状态
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzOTYxXzIwMjAwMjEzMDU1NjM5NjMucG5n?x-oss-process=image/format,png)

- h == null
Head还没初始化。初始时 head == null，第一个节点入队，Head会被初始化一个虚节点。所以说，这里如果还没来得及入队，就会出现head == null
- h != null && waitStatus == 0
后继节点对应的线程仍在运行中，不需要唤醒
- h != null && waitStatus < 0
后继节点可能被阻塞了，需要唤醒

### unparkSuccessor
```java
    private void unparkSuccessor(Node node) {
        /*
         * 如果状态是负数的（即可能需要signal），请尝试清除预期的signal。 如果失败或状态被等待线程更改，则OK。
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * 要unpark的线程保留在后继线程中，后者通常就是下一个节点。 但是，如果取消或显然为空，从尾部逆向移动以找到实际的未取消后继者。
         */
        Node s = node.next;
        // 如果下个节点为 null 或者 cancelled，就找到队列最开始的非cancelled 的节点
        if (s == null || s.waitStatus > 0) {
            s = null;
            // 从尾部节点开始到队首方向查找，寻得队列第一个 waitStatus<0 的节点。
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        // 如果下个节点非空，而且unpark状态<=0的节点
        if (s != null)
            LockSupport.unpark(s.thread);
    }
```

- 之前的addWaiter方法的节点入队并不是原子操作
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzODU1XzIwMjAwMjEzMTk0NjM3MTAyLnBuZw?x-oss-process=image/format,png)
标识部分可以看做是 tail 入队的原子操作，但是此时`pred.next = node;`尚未执行，如果这个时候执行了`unparkSuccessor`，就无法从前往后找了
- 在产生`CANCELLED`状态节点的时候，先断开的是 next 指针，prev 指针并未断开，因此也是必须要从后往前遍历才能够遍历完

## 7.2 releaseShared
以共享模式释放。 如果 *tryReleaseShared(int)*  返回true，则通过解除一个或多个线程的阻塞来实现。

arg 参数 - 该值传送给 tryReleaseShared（int），但并未实现，可以自定义喜欢的任何内容。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTY0Mzc4XzIwMjAwMjEzMjExMjI2NTA2LnBuZw?x-oss-process=image/format,png)
### 执行流程
1. *tryReleaseShared*  尝试释放共享锁，失败返回 *false*，true 成功走2
2. 唤醒当前节点的后续阻塞节点

### doReleaseShared
共享模式下的释放动作 - 表示后继信号并确保传播（注意：对于独占模式，如果需要signal，释放仅相当于调用head的unparkSuccessor）。
```java
    private void doReleaseShared() {
        /*
         * 即使有其他正在进行的acquire/release，也要确保 release 传播。 
         * 如果需要signal，则以尝试 unparkSuccessor head节点的常规方式进行。
         * 但如果没有，则将状态设置为 PROPAGATE，以确保释放后继续传播。
         * 此外，在执行此操作时，必须循环以防添加新节点。 
         * 另外，与unparkSuccessor的其他用法不同，我们需要知道CAS重置状态是否失败，如果重新检查，则失败。
         */
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // 循环以重新检查
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // 在失败的CAS上循环
            }
            if (h == head)                   // 如果头结点变了则循环
                break;
        }
    }
```

# 8 中断处理
唤醒对应线程后，对应的线程就会继续往下执行。继续执行`acquireQueued`方法以后，中断如何处理？

## 8.1 parkAndCheckInterrupt
park 的便捷方法，然后检查是否中断
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzODkyXzIwMjAwMjEzMTk1NTU1NjU3LnBuZw?x-oss-process=image/format,png)
- 再看回 *acquireQueued* 代码，不论 *parkAndCheckInterrupt* 返回什么，都会执行下次循环。若此时获取锁成功，就返回当前的 *interrupted*。
![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTY0NDU4XzIwMjAwMjEzMjAzMDM2NjExLnBuZw?x-oss-process=image/format,png)
- *acquireQueued* 为True，就会执行 *selfInterrupt*
![](https://img-blog.csdnimg.cn/20200509093010679.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNTg5NTEw,size_1,color_FFFFFF,t_70)
## 8.2 selfInterrupt![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly91cGxvYWRmaWxlcy5ub3djb2Rlci5jb20vZmlsZXMvMjAyMDAyMTMvNTA4ODc1NV8xNTgxNjAxMTYzOTc3XzIwMjAwMjEzMjAzODQ2ODE1LnBuZw?x-oss-process=image/format,png)
该方法是为了中断线程。

获取锁后还要中断线程的原因：
- 当中断线程被唤醒时，并不知道被唤醒的原因，可能是当前线程在等待中被中断，也可能释放锁后被唤醒。因此通过 *Thread.interrupted()* 检查中断标识并记录，如果发现该线程被中断过，就再中断一次
- 线程在等待资源的过程中被唤醒，唤醒后还是会不断尝试获取锁，直到抢到锁。即在整个流程中，并不响应中断，只是记录中断的记录。最后抢到锁返回了，那么如果被中断过的话，就需要补充一次中断