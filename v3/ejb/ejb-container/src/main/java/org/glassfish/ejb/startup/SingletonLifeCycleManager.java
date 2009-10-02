package org.glassfish.ejb.startup;

import com.sun.ejb.containers.AbstractSingletonContainer;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;

import java.util.*;

/**
 * @author Mahesh Kannan
 *         Date: Aug 6, 2008
 */
public class SingletonLifeCycleManager {

    Set<String> names = new HashSet<String>();

    Map<String, Set<String>> initialDependency =
            new HashMap<String, Set<String>>();

    Map<String, Integer> name2Index = new HashMap<String, Integer>();

    Map<Integer, String> index2Name = new HashMap<Integer, String>();

    Set<String> leafNodes = new HashSet<String>();

    int maxIndex = 0;

    boolean adj[][];

    // List of eagerly initialized singletons, in the order they were
    // initialized.
    List<AbstractSingletonContainer> initializedSingletons =
            new ArrayList<AbstractSingletonContainer>();

    private Map<String, AbstractSingletonContainer> name2Container =
            new HashMap<String, AbstractSingletonContainer>();

    public SingletonLifeCycleManager() {

    }

    public void addSingletonContainer(AbstractSingletonContainer c) {
        c.setSingletonLifeCycleManager(this);
        EjbSessionDescriptor sdesc = (EjbSessionDescriptor) c.getEjbDescriptor();
        String modName = sdesc.getEjbBundleDescriptor().getName();
        //System.out.println("BundleName: " + modName);
        String src = normalizeSingletonName(sdesc.getName(), sdesc);
        
        String[] depends = sdesc.getDependsOn();
        String[] newDepends = new String[depends.length];

        for(int i=0; i < depends.length; i++) {
            newDepends[0] = normalizeSingletonName(depends[i], sdesc);
        }
        this.addDependency(src, newDepends);

        name2Container.put(src, (AbstractSingletonContainer) c);
    }

    private String normalizeSingletonName(String origName, EjbSessionDescriptor sessionDesc) {

        String normalizedName = origName;

        boolean fullyQualified = origName.contains("#");

        Application app = sessionDesc.getEjbBundleDescriptor().getApplication();

        if( fullyQualified ) {

            int indexOfHash = origName.indexOf("#");
            String ejbName = origName.substring(indexOfHash+1);
            String relativeJarPath = origName.substring(0, indexOfHash);

            BundleDescriptor bundle = app.getRelativeBundle(sessionDesc.getEjbBundleDescriptor(),
                        relativeJarPath);

            if( bundle == null ) {
                throw new IllegalStateException("Invalid @DependOn value = " + origName +
                        " for Singleton " + sessionDesc.getName());
            }

            normalizedName = bundle.getModuleDescriptor().getArchiveUri() + "#" + ejbName;

        } else {

            normalizedName = sessionDesc.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri() +
                    "#" + origName;

        }

        return normalizedName;

    }

    public void doStartup() {
        AbstractSingletonContainer[] partialOrder = this.getPartiallyOrderedSingletonDescriptors();
        int orderSz = partialOrder.length;

        for (int i = 0; i < orderSz; i++) {
            EjbSessionDescriptor sdesc = (EjbSessionDescriptor) partialOrder[i].getEjbDescriptor();

            String normalizedSingletonName = normalizeSingletonName(sdesc.getName(), sdesc);

            if (sdesc.getInitOnStartup()) {
                initializeSingleton(name2Container.get(normalizedSingletonName));
            }
        }
    }

    public void doShutdown() {

        // Shutdown singletons in the reverse order of their initialization
        Collections.reverse(initializedSingletons);

        for(AbstractSingletonContainer singletonContainer : initializedSingletons) {

            singletonContainer.onShutdown();

        }

        return;
    }

    public synchronized void initializeSingleton(AbstractSingletonContainer c) {
        if (! initializedSingletons.contains(c)) {
            String normalizedSingletonName = normalizeSingletonName(c.getEjbDescriptor().getName(),
                    (EjbSessionDescriptor) c.getEjbDescriptor());
            List<String> computedDeps = computeDependencies(normalizedSingletonName);
            int sz = computedDeps.size();
            AbstractSingletonContainer[] deps = new AbstractSingletonContainer[sz];
            for (int i = 0; i < sz; i++) {
                deps[i] = (AbstractSingletonContainer)
                        name2Container.get(computedDeps.get(i));

                initializeSingleton(deps[i]);
            }

            c.instantiateSingletonInstance();
            initializedSingletons.add(c);
        }
    }

    public void addDependency(String src, String[] depends) {
        if (depends != null && depends.length > 0) {
            for (String s : depends) {
                addDependency(src, s);
            }
        } else {
            addDependency(src, "");
        }
    }

    public void addDependency(String src, List<String> depends) {
        if (depends != null) {
            for (String s : depends) {
                addDependency(src, s);
            }
        } else {
            addDependency(src, "");
        }
    }

    public void addDependency(String src, String depends) {
        src = src.trim();

        Set<String> deps = getExistingDependecyList(src);
        StringTokenizer tok = new StringTokenizer(depends, " ,");
        while (tok.hasMoreTokens()) {
            String dep = tok.nextToken();
            deps.add(dep);
            getExistingDependecyList(dep);
        }
    }

    public String[] getPartialOrdering() {
        if (adj == null) {
            fillAdjacencyMatrix();
        }
        boolean[][] tempAdj = new boolean[maxIndex][maxIndex];
        for (int i = 0; i < maxIndex; i++) {
            for (int j = 0; j < maxIndex; j++) {
                tempAdj[i][j] = adj[i][j];
            }
        }
        List<String> dependencies = new ArrayList<String>();
        do {
            String src = null;
            boolean foundAtLeastOneLeaf = false;
            for (int i = 0; i < maxIndex; i++) {
                src = index2Name.get(i);
                if (!dependencies.contains(src)) {
                    boolean hasDep = false;
                    for (int j = 0; j < maxIndex; j++) {
                        if (tempAdj[i][j]) {
                            hasDep = true;
                            break;
                        }
                    }

                    if ((!hasDep) && (!dependencies.contains(src))) {
                        dependencies.add(src);
                        for (int k = 0; k < maxIndex; k++) {
                            tempAdj[k][i] = false;
                        }
                        foundAtLeastOneLeaf = true;
                    }
                }
            }

            if ((!foundAtLeastOneLeaf) && (dependencies.size() < name2Index.size())) {
                throw new IllegalArgumentException("Circular dependency: "
                        + getCyclicString(tempAdj));
            }

        } while (dependencies.size() < name2Index.size());

        return dependencies.toArray(new String[0]);

    }

    public AbstractSingletonContainer[] getPartiallyOrderedSingletonDescriptors() {
        String[] computedDeps = getPartialOrdering();
        int sz = computedDeps.length;
        AbstractSingletonContainer[] deps = new AbstractSingletonContainer[sz];
        for (int i = 0; i < sz; i++) {
            deps[i] = (AbstractSingletonContainer)
                    name2Container.get(computedDeps[i]);
        }

        return deps;
    }

    public List<String> computeDependencies(String root) {
        if (adj == null) {
            fillAdjacencyMatrix();
        }
        Stack<String> stk = new Stack<String>();
        stk.push(root);
        List<String> dependencies = new ArrayList<String>();
        do {
            String top = stk.peek();
            int topIndex = name2Index.get(top);
            boolean hasDep = false;

            for (int j = 0; j < maxIndex; j++) {
                if (adj[topIndex][j]) {
                    String name = index2Name.get(j);
                    if (stk.contains(name)) {
                        String str = "Cyclic dependency: "
                                + top + " => " + name + "? ";
                        throw new IllegalArgumentException(
                                str + getCyclicString(adj));
                    } else {
                        if (!dependencies.contains(name)) {
                            if (leafNodes.contains(name)) {
                                dependencies.add(name);
                            } else {
                                hasDep = true;
                                stk.push(name);
                            }
                        }
                    }
                }
            }
            if (!hasDep) {
                stk.pop();
                if (!dependencies.contains(top)) {
                    dependencies.add(top);
                }
            }
        } while (!stk.empty());

        dependencies.remove(dependencies.size() - 1);
        
        return dependencies;
    }

    private Set<String> getExistingDependecyList(String src) {
        Set<String> existingDeps = initialDependency.get(src);
        if (existingDeps == null) {
            existingDeps = new HashSet<String>();
            initialDependency.put(src, existingDeps);
            name2Index.put(src, maxIndex);
            index2Name.put(maxIndex, src);
            maxIndex++;
        }

        return existingDeps;
    }

    private void fillAdjacencyMatrix() {
        adj = new boolean[maxIndex][maxIndex];
        for (int i = 0; i < maxIndex; i++) {
            String src = index2Name.get(i);
            for (int j = 0; j < maxIndex; j++) {
                adj[i][j] = false;
            }

            boolean isLeaf = true;
            Set<String> deps = initialDependency.get(src);
            for (String d : deps) {
                int k = name2Index.get(d);
                adj[i][k] = true;
                isLeaf = false;
            }

            if (isLeaf) {
                leafNodes.add(src);
            }
        }
    }

    private String getCyclicString(boolean[][] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxIndex; i++) {
            StringBuilder sb2 = new StringBuilder("");
            String delim = "";
            for (int j = 0; j < maxIndex; j++) {
                if (a[i][j]) {
                    sb2.append(delim).append(index2Name.get(j));
                    delim = ", ";
                }
            }
            String dep = sb2.toString();
            if (dep.length() > 0) {
                sb.append(" ").append(index2Name.get(i))
                        .append(" => ").append(sb2.toString()).append("; ");
            }

        }
        return sb.toString();
    }

    public void printAdjacencyMatrix() {
        if (adj == null) {
            fillAdjacencyMatrix();
        }
        System.out.print(" ");
        for (int i = 0; i < maxIndex; i++) {
            System.out.print(" " + index2Name.get(i));
        }
        System.out.println();
        for (int i = 0; i < maxIndex; i++) {
            System.out.print(index2Name.get(i) + " ");
            for (int j = 0; j < maxIndex; j++) {
                System.out.print(adj[i][j] ? "1 " : "0 ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        //test1();
        //test2();
        //test3();
        //test4();
    }


    private static void test1() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager();
        ts.addDependency("A", "B, C");
        ts.addDependency("B", "C, D");
        ts.addDependency("D", "E");
        ts.addDependency("C", "D, E, G");
        ts.addDependency("E", "D");

        ts.getPartialOrdering();

        SingletonLifeCycleManager t = new SingletonLifeCycleManager();
        t.addDependency("C", ts.computeDependencies("C"));
        t.addDependency("D", ts.computeDependencies("D"));

        t.printAdjacencyMatrix();

        for (String s : t.computeDependencies("C")) {
            System.out.print(s + " ");
        }
        System.out.println();

        for (String s : t.getPartialOrdering()) {
            System.out.print(s + " ");
        }

    }

    private static void test2() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager();
        ts.addDependency("A", "D, E");
        ts.addDependency("B", "F");
        ts.addDependency("C", "G, H");
        ts.addDependency("D", "I");
        ts.addDependency("E", "J");
        ts.addDependency("F", "J, K");
        ts.addDependency("H", "L");
        ts.addDependency("I", "M, N");
        ts.addDependency("J", "U");
        ts.addDependency("K", "U");
        ts.addDependency("L", "O");
        ts.addDependency("U", "N, O");
        ts.addDependency("N", "P, Q");
        ts.addDependency("O", "Q, R");
        ts.addDependency("Q", "S, T");

        ts.addDependency("E", "X, W");
        ts.addDependency("X", "Y");
        ts.addDependency("W", "Y");
        ts.addDependency("Y", "Z");
        ts.addDependency("Z", "O");
        //ts.addDependency("R", "J");

        String[] dep = ts.getPartialOrdering();
        for (String s : ts.getPartialOrdering()) {
            System.out.print(s + " ");
        }
        System.out.println();

        SingletonLifeCycleManager ts2 = new SingletonLifeCycleManager();
        ts2.addDependency("E", ts.computeDependencies("E"));
        ts2.addDependency("U", ts.computeDependencies("U"));
        ts2.addDependency("H", ts.computeDependencies("H"));
        String[] dep2 = ts2.getPartialOrdering();
        for (String s : ts2.getPartialOrdering()) {
            System.out.print(s + " ");
        }
    }

    private static void test3() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager();
        ts.addDependency("A", (String) null);
        ts.addDependency("B", (String) null);
        ts.addDependency("C", (String) null);

        String[] dep = ts.getPartialOrdering();
        for (String s : ts.getPartialOrdering()) {
            System.out.print(s + " ");
        }
        System.out.println();
        for (String s : ts.computeDependencies("B")) {
            System.out.print(s + " ");
        }
        System.out.println();
    }

    private static void test4() {
        SingletonLifeCycleManager ts = new SingletonLifeCycleManager();
        ts.addDependency("A", "D, B, C");
        ts.addDependency("B", "F, I");
        ts.addDependency("C", "F, H, G");
        ts.addDependency("D", "E");
        ts.addDependency("E", (String) null);
        ts.addDependency("F", "E");
        ts.addDependency("G", "E, I, K");
        ts.addDependency("H", "J");
        ts.addDependency("I", "J");
        ts.addDependency("K", (List) null);

        String[] dep = ts.getPartialOrdering();
        for (String s : ts.getPartialOrdering()) {
            System.out.print(s + " ");
        }
        System.out.println();
        for (String s : ts.computeDependencies("C")) {
            System.out.print(s + " ");
        }
        System.out.println();
    }

}
