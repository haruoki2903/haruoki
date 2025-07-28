package store.view;

import store.model.SanPham;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductCardPanel extends JPanel {
    // --- Hằng số Style ---
    private static final Font FONT_PRODUCT_NAME = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_SUPPLIER = new Font("Arial", Font.ITALIC, 13); // Font cho NCC
    private static final Font FONT_PRODUCT_PRICE = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    
    private static final Color COLOR_BUTTON_BG = new Color(34, 34, 34);
    private static final Color COLOR_TEXT_DARK = new Color(34, 34, 34);
    private static final Color COLOR_TEXT_LIGHT = new Color(102, 102, 102);
    
    private static final Dimension CARD_SIZE = new Dimension(240, 350);
    
    private static final Border BORDER_NORMAL = BorderFactory.createLineBorder(new Color(230, 230, 230));
    private static final Border BORDER_HOVER = BorderFactory.createLineBorder(new Color(128, 0, 0), 2);

    public ProductCardPanel(SanPham sanPham, ActionListener addToCartAction) {
        // --- 1. Cấu hình Panel chính ---
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(CARD_SIZE);
        setMaximumSize(CARD_SIZE);
        setBorder(BORDER_NORMAL);
        setBackground(Color.WHITE);

        // --- 2. Hình ảnh sản phẩm ---
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        URL imageUrl = getClass().getResource("/image/sample.png");
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            imageLabel.setText("Không tìm thấy ảnh sample.png");
            imageLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.LIGHT_GRAY);
        }

        // --- 3. Panel thông tin ---
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        infoPanel.setOpaque(false);

        // <<<< THÊM HIỂN THỊ NHÀ CUNG CẤP >>>>
        JLabel supplierLabel = new JLabel(sanPham.getTenNhaCungCap() == null ? " " : sanPham.getTenNhaCungCap());
        supplierLabel.setFont(FONT_SUPPLIER);
        supplierLabel.setForeground(COLOR_TEXT_LIGHT);

        JLabel nameLabel = new JLabel("<html><body style='width: 180px'>" + sanPham.getTen() + "</body></html>");
        nameLabel.setFont(FONT_PRODUCT_NAME);
        nameLabel.setForeground(COLOR_TEXT_DARK);

        JPanel topInfoPanel = new JPanel();
        topInfoPanel.setOpaque(false);
        topInfoPanel.setLayout(new BoxLayout(topInfoPanel, BoxLayout.Y_AXIS));
        topInfoPanel.add(supplierLabel);
        topInfoPanel.add(nameLabel);

        JLabel priceLabel = new JLabel(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(sanPham.getDonGia()));
        priceLabel.setFont(FONT_PRODUCT_PRICE);
        priceLabel.setForeground(COLOR_TEXT_LIGHT);

        infoPanel.add(topInfoPanel, BorderLayout.NORTH);
        infoPanel.add(priceLabel, BorderLayout.SOUTH);
        
        // --- 4. Nút "Thêm vào giỏ" ---
        JButton addButton = new JButton("Thêm vào giỏ");
        styleAddButton(addButton);
        addButton.setActionCommand(sanPham.getMaSP());
        addButton.addActionListener(addToCartAction);
        
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        buttonWrapper.add(addButton, BorderLayout.CENTER);

        // --- 5. Lắp ráp ---
        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(infoPanel);
        southPanel.add(buttonWrapper);
        
        add(imageLabel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        // --- 6. Hiệu ứng Hover ---
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBorder(BORDER_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { setBorder(BORDER_NORMAL); }
        };
        addMouseListener(hoverAdapter);
        imageLabel.addMouseListener(hoverAdapter);
        infoPanel.addMouseListener(hoverAdapter);
        southPanel.addMouseListener(hoverAdapter);
    }

    private void styleAddButton(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_BUTTON_BG);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 40));
    }
}