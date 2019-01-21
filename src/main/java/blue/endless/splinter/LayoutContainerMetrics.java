package blue.endless.splinter;

public class LayoutContainerMetrics {
	protected int cellPadding;
	protected boolean collapseMargins;

	public LayoutContainerMetrics setCellPadding(int amount) {
		this.cellPadding = amount;
		return this;
	}
	
	public LayoutContainerMetrics setCollapseMargins(boolean collapseMargins) {
		this.collapseMargins = collapseMargins;
		return this;
	}
}
