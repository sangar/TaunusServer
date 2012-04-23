/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.gasa.master;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JSlider;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author gard
 */
public class TaunusServerUI extends javax.swing.JFrame {

    private Server server;
    private ArrayList<Connection> connlist;
    private boolean isConnected;
    private boolean isPlaying;
    DefaultListModel connListModel = new DefaultListModel();
    
    /*
     * JFreeChart components
     */
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private XYSeriesCollection dataset;
    private XYSeries series1;
    private XYSeries series2;
    private XYSeries series3;
    private XYSeries series4;

    // <editor-fold defaultstate="collapsed" desc="Create dataset">
    private XYDataset createDataset() {
        series1 = new XYSeries("First");
        /*
         * series1.add(1.0, 1.0); series1.add(2.0, 4.0); series1.add(3.0, 3.0);
         * series1.add(4.0, 5.0); series1.add(5.0, 5.0); series1.add(6.0, 7.0);
         * series1.add(7.0, 7.0); series1.add(8.0, 8.0);
         */
        series2 = new XYSeries("Second");
        /*
         * series2.add(1.0, 5.0); series2.add(2.0, 7.0); series2.add(3.0, 6.0);
         * series2.add(4.0, 8.0); series2.add(5.0, 4.0); series2.add(6.0, 4.0);
         * series2.add(7.0, 2.0); series2.add(8.0, 1.0);
         */
        series3 = new XYSeries("Third");
        /*
         * series3.add(3.0, 4.0); series3.add(4.0, 3.0); series3.add(5.0, 2.0);
         * series3.add(6.0, 3.0); series3.add(7.0, 6.0); series3.add(8.0, 3.0);
         * series3.add(9.0, 4.0); series3.add(10.0, 3.0); // series3.add(100.0,
         * 2.0);
         */
        series4 = new XYSeries("Fourth");
        /*
         * series4.add(2.0, 3.0); series4.add(3.0, 2.0); series4.add(4.0, 1.0);
         * series4.add(5.0, 2.0); series4.add(6.0, 5.0); series4.add(7.0, 2.0);
         * series4.add(8.0, 3.0); series4.add(9.0, 2.0);
         */
        dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        dataset.addSeries(series4);

        return dataset;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Create linechart">
    private JFreeChart createChart(XYDataset dataset, String title) {

        JFreeChart linechart = ChartFactory.createXYLineChart(title, null/*
                 * "X"
                 */, null/*
                 * "Y"
                 */, dataset,
                PlotOrientation.VERTICAL, false, true, false);

        linechart.setBackgroundPaint(new Color(213, 213, 213));
        
        final XYPlot plot = linechart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

//        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//        renderer.setSeriesLinesVisible(0, false);
//        renderer.setSeriesShapesVisible(1, false);
//        plot.setRenderer(renderer);
        ValueAxis axis = plot.getDomainAxis();
//        axis.setAutoRange(true);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(0.0, 1024.0);

        return linechart;
    }// </editor-fold>
    
    /*
     * Balancepoint components
     */
    private BalancePointCanvas balancePointCanvas;
    private Point leftFront = new Point(0, 0), leftBack = new Point(0, 0),
            rightFront = new Point(0, 0), rightBack = new Point(0, 0);

    /*
     * Balancepoint view
     */
    // <editor-fold defaultstate="collapsed" desc="Private class BalancePointCanvas">
    private class BalancePointCanvas extends Canvas {

        private Ellipse2D point;
        private int fX, fY;
        
        private void init(Dimension d) {
            int w = (int) d.getWidth();
            int h = (int) d.getHeight();
            point = new Ellipse2D.Float(0, 0, 10, 10);
            centerAround(w / 2 /* - 5 */, h / 2 /* + 5 */, point);
            setPosition(0, 0);
        }

        public void setPosition(int x, int y) {
            this.fX = x;
            this.fY = y;

            this.repaint();
        }

        private void centerAround(int x, int y, Ellipse2D g) {
            int gw = (int) g.getWidth();
            int gh = (int) g.getHeight();
            int left = x - gw / 2;
            int top = y - gh / 2;
            int right = left + gw;
            int bottom = top + gh;

            g.setFrame(new Rectangle(left, top, 10, 10));
//            System.out.printf("%d %d %d %d\n", left, top, right, bottom);
        }

        @Override
        public void setSize(Dimension d) {
            super.setSize(d);
            this.init(d);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            Graphics2D g2D = (Graphics2D) g;

            int w = this.getWidth();
            int h = this.getHeight();
            
            g2D.setColor(new Color(223, 223, 223));
            g2D.fillRect(0, 0, w, h);

            /*
             * black rings
             */
            g2D.setColor(Color.black);
            //g2D.draw(new Ellipse2D.Float(w/2.5f, h/2.5f, w/5.f, h/5.f));
            //g2D.draw(new Ellipse2D.Float(w/3.f, h/3.f, w/3.f, h/3.f));
            g2D.draw(new Ellipse2D.Float(w / 4.f, h / 4.f, w / 2.f, h / 2.f));
            //g2D.draw(new Ellipse2D.Float(w/6.f, h/6.f, w/1.5f, h/1.5f));
            //g2D.draw(new Ellipse2D.Float(w/10.f, h/10.f, w/1.25f, h/1.25f));
            g2D.draw(new Ellipse2D.Float(0, 0, w, h));

            /* bullseye 
             * g2D.setColor(Color.red); 
             * g2D.fill(new Ellipse2D.Float(0, 0, w, h)); 
             * g2D.setColor(Color.white); 
             * g2D.fill(new Ellipse2D.Float(w/10.f, h/10.f, w/1.25f, h/1.25f));
             * g2D.setColor(Color.red); 
             * g2D.fill(new Ellipse2D.Float(w/6.f, h/6.f, w/1.5f, h/1.5f)); 
             * g2D.setColor(Color.white); 
             * g2D.fill(new Ellipse2D.Float(w/4.f, h/4.f, w/2.f, h/2.f));
             * g2D.setColor(Color.red); 
             * g2D.fill(new Ellipse2D.Float(w/3.f, h/3.f, w/3.f, h/3.f)); 
             * g2D.setColor(Color.white); 
             * g2D.fill(new Ellipse2D.Float(w/2.5f, h/2.5f, w/5.f, h/5.f));
             * 
             * g2D.setColor(Color.black);
             */

            // Draw lines
            g2D.draw(new Line2D.Float(w / 2, 0, w / 2, h));
            g2D.draw(new Line2D.Float(0, h / 2, w, h / 2));

            // Draw point
            Ellipse2D p = point;
            int x = w / 2 /* - 5 */ + fX;
            int y = h / 2 /* + 5 */ + fY;
            centerAround(x, y, p);

            g2D.fill(p);
        }
    }// </editor-fold>

    /**
     * ThreadPoolExecutor
     */
    BlockingQueue queue;
    PausableThreadPoolExecutor pausableExecutor;
    
    // <editor-fold defaultstate="collapsed" desc="PausableThreadPoolExecutor">
    private class PausableThreadPoolExecutor extends ThreadPoolExecutor {

        private boolean isPaused;
        private ReentrantLock pauseLock = new ReentrantLock();
        private Condition unpaused = pauseLock.newCondition();
        
        public PausableThreadPoolExecutor(int i, int i1, long l, TimeUnit tu, BlockingQueue<Runnable> bq) {
            super(i, i1, l, tu, bq);
        }

        @Override
        protected void beforeExecute(Thread thread, Runnable r) {
            super.beforeExecute(thread, r);
            pauseLock.lock();
            try {
                while (isPaused) unpaused.await();
                
                System.out.println("Before execute called");
            } catch(InterruptedException e) {
                thread.interrupt();
            } finally {
                pauseLock.unlock();
            }
        }
    
        public void pause() {
            pauseLock.lock();
            try {
                isPaused = true;
            } finally {
                pauseLock.unlock();
            }
        }
        
        public void resume() {
            pauseLock.lock();
            try {
                isPaused = false;
                unpaused.signalAll();
            } finally {
                pauseLock.unlock();
            }
        }
    }// </editor-fold>
    
    /**
     * Creates new form TaunusServerUI
     */
    public TaunusServerUI() {
        super("Server");

        isConnected = false;
        isPlaying = false;

        initComponents();
        initChart();
        initBalancePointCanvas();
        initConnectionList();
        initServer();
//        initPausableExecutor();
        setConnectedStatus(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ioPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        inputTextField = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        disconnectButton = new javax.swing.JButton();
        buttonPanel = new javax.swing.JPanel();
        startRecButton = new javax.swing.JButton();
        stopRecButton = new javax.swing.JButton();
        startStreamButton = new javax.swing.JButton();
        stopStreamButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        linechartContainerPanel = new javax.swing.JPanel();
        progressSlider = new javax.swing.JSlider();
        playButton = new javax.swing.JButton();
        lineChartPanel = new javax.swing.JPanel();
        palancePointContainerPanel = new javax.swing.JPanel();
        balancePointPanel = new javax.swing.JPanel();
        connectionListContainerPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        connectionsJList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(213, 213, 213));
        setPreferredSize(new java.awt.Dimension(1200, 800));

        ioPanel.setBackground(new java.awt.Color(213, 213, 213));

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setFocusable(false);
        jScrollPane1.setRequestFocusEnabled(false);

        outputTextArea.setColumns(20);
        outputTextArea.setEditable(false);
        outputTextArea.setRows(5);
        outputTextArea.setFocusable(false);
        jScrollPane1.setViewportView(outputTextArea);

        inputTextField.setToolTipText("");
        inputTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTextFieldActionPerformed(evt);
            }
        });

        sendButton.setText("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Status: Disconnected");

        disconnectButton.setText("Disconnect");
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout ioPanelLayout = new org.jdesktop.layout.GroupLayout(ioPanel);
        ioPanel.setLayout(ioPanelLayout);
        ioPanelLayout.setHorizontalGroup(
            ioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ioPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ioPanelLayout.createSequentialGroup()
                        .add(inputTextField)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendButton))
                    .add(ioPanelLayout.createSequentialGroup()
                        .add(ioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1)
                            .add(ioPanelLayout.createSequentialGroup()
                                .add(statusLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(disconnectButton)))
                        .addContainerGap())))
        );
        ioPanelLayout.setVerticalGroup(
            ioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ioPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusLabel)
                    .add(disconnectButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ioPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inputTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendButton))
                .addContainerGap())
        );

        buttonPanel.setBackground(new java.awt.Color(213, 213, 213));
        buttonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Control"));

        startRecButton.setText("Start");
        startRecButton.setEnabled(false);
        startRecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startRecButtonActionPerformed(evt);
            }
        });

        stopRecButton.setText("Stop");
        stopRecButton.setEnabled(false);
        stopRecButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopRecButtonActionPerformed(evt);
            }
        });

        startStreamButton.setText("Start");
        startStreamButton.setEnabled(false);
        startStreamButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStreamButtonActionPerformed(evt);
            }
        });

        stopStreamButton.setText("Stop");
        stopStreamButton.setEnabled(false);
        stopStreamButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopStreamButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Record");

        jLabel2.setText("Stream");

        org.jdesktop.layout.GroupLayout buttonPanelLayout = new org.jdesktop.layout.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(buttonPanelLayout.createSequentialGroup()
                        .add(startRecButton)
                        .add(18, 18, 18)
                        .add(stopRecButton))
                    .add(buttonPanelLayout.createSequentialGroup()
                        .add(startStreamButton)
                        .add(18, 18, 18)
                        .add(stopStreamButton))
                    .add(jLabel1)
                    .add(jLabel2))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startRecButton)
                    .add(stopRecButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startStreamButton)
                    .add(stopStreamButton))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        linechartContainerPanel.setBackground(new java.awt.Color(213, 213, 213));
        linechartContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Linechart"));

        progressSlider.setValue(0);
        progressSlider.setEnabled(false);

        playButton.setText("Play");
        playButton.setEnabled(false);
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });

        lineChartPanel.setBackground(new java.awt.Color(213, 213, 213));

        org.jdesktop.layout.GroupLayout lineChartPanelLayout = new org.jdesktop.layout.GroupLayout(lineChartPanel);
        lineChartPanel.setLayout(lineChartPanelLayout);
        lineChartPanelLayout.setHorizontalGroup(
            lineChartPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        lineChartPanelLayout.setVerticalGroup(
            lineChartPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout linechartContainerPanelLayout = new org.jdesktop.layout.GroupLayout(linechartContainerPanel);
        linechartContainerPanel.setLayout(linechartContainerPanelLayout);
        linechartContainerPanelLayout.setHorizontalGroup(
            linechartContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(linechartContainerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(linechartContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(linechartContainerPanelLayout.createSequentialGroup()
                        .add(progressSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(playButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lineChartPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        linechartContainerPanelLayout.setVerticalGroup(
            linechartContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(linechartContainerPanelLayout.createSequentialGroup()
                .add(lineChartPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(linechartContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, linechartContainerPanelLayout.createSequentialGroup()
                        .add(progressSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, playButton)))
        );

        palancePointContainerPanel.setBackground(new java.awt.Color(213, 213, 213));
        palancePointContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Balance point"));
        palancePointContainerPanel.setPreferredSize(new java.awt.Dimension(225, 240));

        balancePointPanel.setBackground(new java.awt.Color(213, 213, 213));
        balancePointPanel.setPreferredSize(new java.awt.Dimension(210, 210));

        org.jdesktop.layout.GroupLayout balancePointPanelLayout = new org.jdesktop.layout.GroupLayout(balancePointPanel);
        balancePointPanel.setLayout(balancePointPanelLayout);
        balancePointPanelLayout.setHorizontalGroup(
            balancePointPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 210, Short.MAX_VALUE)
        );
        balancePointPanelLayout.setVerticalGroup(
            balancePointPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 210, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout palancePointContainerPanelLayout = new org.jdesktop.layout.GroupLayout(palancePointContainerPanel);
        palancePointContainerPanel.setLayout(palancePointContainerPanelLayout);
        palancePointContainerPanelLayout.setHorizontalGroup(
            palancePointContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(palancePointContainerPanelLayout.createSequentialGroup()
                .add(balancePointPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        palancePointContainerPanelLayout.setVerticalGroup(
            palancePointContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(palancePointContainerPanelLayout.createSequentialGroup()
                .add(balancePointPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        connectionListContainerPanel.setBackground(new java.awt.Color(213, 213, 213));
        connectionListContainerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connections"));

        connectionsJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(connectionsJList);

        org.jdesktop.layout.GroupLayout connectionListContainerPanelLayout = new org.jdesktop.layout.GroupLayout(connectionListContainerPanel);
        connectionListContainerPanel.setLayout(connectionListContainerPanelLayout);
        connectionListContainerPanelLayout.setHorizontalGroup(
            connectionListContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        connectionListContainerPanelLayout.setVerticalGroup(
            connectionListContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(connectionListContainerPanelLayout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ioPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(linechartContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, connectionListContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, palancePointContainerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(palancePointContainerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(connectionListContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(linechartContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ioPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(buttonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Init lineChart">
    private void initChart() {
        chart = createChart(createDataset(), null/*
                 * Title
                 */);
        chartPanel = new ChartPanel(chart);
        chartPanel.setSize(lineChartPanel.getSize());

        /*
         * Set the chartPanel resizeble
         */
        org.jdesktop.layout.GroupLayout lineChartPanelLayout = new org.jdesktop.layout.GroupLayout(lineChartPanel);
        lineChartPanel.setLayout(lineChartPanelLayout);
        lineChartPanelLayout.setHorizontalGroup(
                lineChartPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lineChartPanelLayout.createSequentialGroup().addContainerGap().add(lineChartPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(chartPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        lineChartPanelLayout.setVerticalGroup(
                lineChartPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lineChartPanelLayout.createSequentialGroup().add(chartPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED).add(lineChartPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING))));

//        lineChartPanel.add(chartPanel);
//        lineChartPanel.getParent().validate();
    } // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Init balancePointCanvas">
    private void initBalancePointCanvas() {
        balancePointCanvas = new BalancePointCanvas();
        balancePointCanvas.setSize(balancePointPanel.getSize());
        balancePointPanel.add(balancePointCanvas);
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Init connectionList">
    private void initConnectionList() {
        connListModel.addElement("No connections");
        connectionsJList.setModel(connListModel);
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Init server">
    private void initServer() {
        server = new Server(this);
        server.runServer(14253); // Default 14253
        this.connlist = server.getConnlist();
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Init PausableExecutor">
    private void initPausableExecutor() {
//        queue = new SynchronousQueue();
//        pausableExecutor = new PausableThreadPoolExecutor(1, 100, 100, TimeUnit.SECONDS, queue);
    }// </editor-fold>
    
    /**
     * Auto-generated button actions
     */
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        String input = inputTextField.getText();

        // send input to phone
        try {
            Connection c = connlist.get(connectionsJList.getSelectedIndex());
            server.sendString(input, c);
        } catch (Exception e) {
            Logger.getLogger(TaunusServerUI.class.getName()).log(Level.SEVERE, null, e);
        }
        setTextToDisplay(String.format("You: %s", input));
    }//GEN-LAST:event_sendButtonActionPerformed

    private void inputTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTextFieldActionPerformed
        sendButton.doClick();
    }//GEN-LAST:event_inputTextFieldActionPerformed

    private void startRecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startRecButtonActionPerformed
        if (isConnected) {
            inputTextField.setText("1rstart");
            sendButton.doClick();
            startRecButton.setEnabled(false);
            stopRecButton.setEnabled(true);

            clearXYSeries();
        }
    }//GEN-LAST:event_startRecButtonActionPerformed

    private void stopRecButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopRecButtonActionPerformed
        if (isConnected) {
            inputTextField.setText("1rstop");
            sendButton.doClick();
            startRecButton.setEnabled(true);
            stopRecButton.setEnabled(false);

//            playButton.setEnabled(true);
        }
    }//GEN-LAST:event_stopRecButtonActionPerformed

    private void stopStreamButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopStreamButtonActionPerformed
        if (isConnected) {
            inputTextField.setText("1stop");
            sendButton.doClick();
            startStreamButton.setEnabled(true);
            stopStreamButton.setEnabled(false);

            playButton.setEnabled(true);
        }
    }//GEN-LAST:event_stopStreamButtonActionPerformed

    private void startStreamButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStreamButtonActionPerformed
        if (isConnected) {
            inputTextField.setText("1start");
            sendButton.doClick();
            startStreamButton.setEnabled(false);
            stopStreamButton.setEnabled(true);

            clearXYSeries();
        }
    }//GEN-LAST:event_startStreamButtonActionPerformed

    private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectButtonActionPerformed
        if (isConnected) {
            inputTextField.setText("exit");
            sendButton.doClick();
            setConnectedStatus(false);
        } else {
            setConnectedStatus(true);
        }
    }//GEN-LAST:event_disconnectButtonActionPerformed

    // <editor-fold defaultstate="collapsed" desc="ItemToPlay">
    private class ItemToPlay extends Thread {

        private int i;
        private XYPlot plot;
        private XYDataItem item1;
        private XYDataItem item2; 
        private XYDataItem item3;
        private XYDataItem item4;
        private XYLineAnnotation anno;
        private JSlider progressSlider;
        
        public ItemToPlay(int i, XYPlot plot, XYDataItem item1, XYDataItem item2, 
                XYDataItem item3, XYDataItem item4, XYLineAnnotation anno, JSlider progressSlider) {
            this.i = i;
            this.plot = plot;
            this.item1 = item1;
            this.item2 = item2;
            this.item3 = item3;
            this.item4 = item4;
            this.anno = anno;
            this.progressSlider = progressSlider;
        }

        @Override
        public void run() {
            super.run();
            
            setSensorValue(1, (int) item1.getYValue());
            setSensorValue(2, (int) item2.getYValue());
            setSensorValue(3, (int) item3.getYValue());
            setSensorValue(4, (int) item4.getYValue());
            
            plot.addAnnotation(anno);
            progressSlider.setValue(i);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            
            plot.removeAnnotation(anno);
        }
    }// </editor-fold>
    
    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
        
        // Stop the playing
        if (isPlaying) {
            isPlaying = false;
            return;
        }
        
        if (!series1.isEmpty()) {

            isPlaying = true;
            playButton.setText("Stop");

            XYPlot plot = chart.getXYPlot();
            XYLineAnnotation a1;
                        
            int numRecItems = series1.getItemCount();
            progressSlider.setMaximum(numRecItems);
            
            // Tried to setup execution queue
/*         pausableExecutor.setMaximumPoolSize(numRecItems);
                        
            for (int i = 0; i < (numRecItems-10); i++) {
                XYDataItem item1 = series1.getDataItem(i);
                XYDataItem item2 = series2.getDataItem(i);
                XYDataItem item3 = series3.getDataItem(i);
                XYDataItem item4 = series4.getDataItem(i);
                
                a1 = new XYLineAnnotation(i, 0, i, 1024);
                
                queue.add(new ItemToPlay(i, plot, item1, item2, item3, item4, a1, progressSlider));
                
                if (i == 10)
                    break;
            }
*/
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    XYPlot plot = chart.getXYPlot();
//                    XYPlot subplot;
                    XYLineAnnotation a1;
                        
                    int numRecItems = series1.getItemCount()-5;
                    progressSlider.setMaximum(numRecItems);
                        
                    for (int i = 0; i < (numRecItems); i++) {
                        XYDataItem item1 = series1.getDataItem(i);
                        XYDataItem item2 = series2.getDataItem(i);
                        XYDataItem item3 = series3.getDataItem(i);
                        XYDataItem item4 = series4.getDataItem(i);

                        setSensorValue(1, (int) item1.getYValue());
                        setSensorValue(2, (int) item2.getYValue());
                        setSensorValue(3, (int) item3.getYValue());
                        setSensorValue(4, (int) item4.getYValue());

                        // Draw vertical line /
                        a1 = new XYLineAnnotation(i, 0, i, 1024);
// redraws the whole line chart - plot.addAnnotation(a1);
                        
//                        subplot = new XYPlot(dataset, null, new NumberAxis("Fourth"), new StandardXYItemRenderer());
//                        subplot.addAnnotation(a1);
                        
                        progressSlider.setValue(i);
                        
                        // @TODO: check out ThreadPoolExecutor
                        try {        
                            sleep(24); // 41
                        } catch (InterruptedException ex) {
                        Logger.getLogger(TaunusServerUI.class.getName()).log(Level.SEVERE,
                                null, ex);
                        }        
                    
// redraws the whole line chart - plot.removeAnnotation(a1);
//                        subplot.removeAnnotation(a1);
                        
                        if (!isPlaying) {
                            break;
                        }
                    }

                    progressSlider.setValue(0);
                    playButton.setText("Play");
                    isPlaying = false;
                }
            }.start();
        }
    }//GEN-LAST:event_playButtonActionPerformed

    /**
     * Private methods
     */
    private void setConnectedStatus(final boolean connected) {
        isConnected = connected;

        String status;
        if (isConnected) {
            try {
                status = String.format("Status: %d client(s) connected", connlist.size());
            } catch (Exception e) {
                status = "Status: Connected";
            }
        } else {
            status = "Status: Disconnected";
        }

        disconnectButton.setEnabled(isConnected);
        statusLabel.setText(status);
        startRecButton.setEnabled(isConnected);
        stopRecButton.setEnabled(false);
        startStreamButton.setEnabled(isConnected);
        stopStreamButton.setEnabled(false);
        sendButton.setEnabled(isConnected);
        inputTextField.requestFocus();

        updateConnectionList();
    }

    private void setTextToDisplay(String input) {
        if (input.length() > 4) {
            String subInput = input.substring(5);
            if (subInput.charAt(0) == '1') {
                input = "You: " + subInput.substring(1);
            }

            if (subInput.equalsIgnoreCase("clear")) {
                outputTextArea.setText("");
                inputTextField.setText("");
                return;
            }
        }

        if (outputTextArea.getText().equalsIgnoreCase("")) {
            outputTextArea.setText(input);
        } else {
            outputTextArea.append("\n" + input);
            outputTextArea.scrollRectToVisible(new Rectangle(0, outputTextArea.getHeight() + 15, 0, 0));
        }

        inputTextField.setText("");
    }

    private void updateConnectionList() {
        if (connlist.size() > 0) {
            connListModel.clear();
            connectionsJList.setEnabled(true);
            for (Connection c : connlist) {
                connListModel.addElement(c);
                connectionsJList.setSelectedIndex(0);
            }
        } else {
            connListModel.clear();
            connectionsJList.setEnabled(false);
            connListModel.addElement("No connections");
        }
    }

    private void clearXYSeries() {
        series1.clear();
        series2.clear();
        series3.clear();
        series4.clear();
        playButton.setEnabled(false);
    }

    private void setSensorValue(int sId, int value) {
        this.onSensorValueReceive(sId, value);
    }

    /**
     * Public methods
     */
    public void onStatusChange(boolean connected) {
        setConnectedStatus(connected);
    }

    public void onTextReceived(String input) {
        setTextToDisplay(input);
    }

    public void onClientDisconnect() {
        setTextToDisplay("Connection disconnected...");
    }

    public void onRecordedDataReceive(ArrayList<Point> rd) {
        clearXYSeries();
        for (Point point : rd) {
            this.setSensorValue(point.x, point.y);
        }
        playButton.setEnabled(true);
    }

    public void onSensorValueReceive(int sId, int sensorValue) {
        int balancePoint = sensorValue / 4;//(127 * sensorValueFromArduino / 1024);
//        double percentValue = (100 * (double) sensorValue / 1024.0);
//        String rawInput = String.valueOf(sensorValue);
//	String percentInput = mSensorValueFormatter.format(percentValue);

        switch (sId) {
            case 0x1:
//		- -
                if (!isPlaying) {
                    series1.add(series1.getItemCount(), sensorValue);
                }
                leftFront.x = -balancePoint;
                leftFront.y = -balancePoint;
                break;
            case 0x2:
//		- +
                if (!isPlaying) {
                    series2.add(series2.getItemCount(), sensorValue);
                }
                leftBack.x = -balancePoint;
                leftBack.y = balancePoint;
                break;
            case 0x3:
//	 	+ -
                if (!isPlaying) {
                    series3.add(series3.getItemCount(), sensorValue);
                }
                rightFront.x = balancePoint;
                rightFront.y = -balancePoint;
                break;
            case 0x4:
//		+ +
                if (!isPlaying) {
                    series4.add(series4.getItemCount(), sensorValue);
                }
                rightBack.x = balancePoint;
                rightBack.y = balancePoint;
                break;
        }

        // midpoint front
        int x1 = (leftFront.x + rightFront.x) / 2;
        int y1 = (leftFront.y + rightFront.y) / 2;

        // midpoint back
        int x2 = (leftBack.x + rightBack.x) / 2;
        int y2 = (leftBack.y + rightBack.y) / 2;

        // midpoint front and back
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        balancePointCanvas.setPosition(x, y);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                /*
                 * Metal, Nimbus, CDE/Motif, Mac OS X
                 */
                if ("Mac OS X".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TaunusServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TaunusServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TaunusServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TaunusServerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new TaunusServerUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel balancePointPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel connectionListContainerPanel;
    private javax.swing.JList connectionsJList;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JTextField inputTextField;
    private javax.swing.JPanel ioPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel lineChartPanel;
    private javax.swing.JPanel linechartContainerPanel;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JPanel palancePointContainerPanel;
    private javax.swing.JButton playButton;
    private javax.swing.JSlider progressSlider;
    private javax.swing.JButton sendButton;
    private javax.swing.JButton startRecButton;
    private javax.swing.JButton startStreamButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton stopRecButton;
    private javax.swing.JButton stopStreamButton;
    // End of variables declaration//GEN-END:variables
}
