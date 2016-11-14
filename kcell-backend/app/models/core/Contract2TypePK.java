package models.core;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class Contract2TypePK implements Serializable {

    private Contract contract;
    private ContractType contractType;

    @ManyToOne
    @JoinColumn(name = "CONTRACT_")
    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    @ManyToOne
    @JoinColumn(name = "CONTRACT_TYPE_")
    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contract == null) ? 0 : contract.hashCode());
        result = prime * result + ((contractType == null) ? 0 : contractType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Contract2TypePK other = (Contract2TypePK) obj;
        if (contract == null) {
            if (other.contract != null) {
                return false;
            }
        } else if (!contract.equals(other.contract)) {
            return false;
        }
        if (contractType == null) {
            if (other.contractType != null) {
                return false;
            }
        } else if (!contractType.equals(other.contractType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getContract().getId() + ":" + this.getContractType().getName();
    }
}
